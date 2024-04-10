package org.comroid.mcsd.core;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.java.Log;
import org.comroid.annotations.Order;
import org.comroid.api.Polyfill;
import org.comroid.api.func.exc.ThrowingFunction;
import org.comroid.api.func.ext.Wrap;
import org.comroid.api.func.util.AlmostComplete;
import org.comroid.api.func.util.Streams;
import org.comroid.api.info.Maintenance;
import org.comroid.api.tree.Component;
import org.comroid.api.tree.UncheckedCloseable;
import org.comroid.mcsd.core.entity.module.ModulePrototype;
import org.comroid.mcsd.core.entity.module.remote.ssh.SshFileModulePrototype;
import org.comroid.mcsd.core.entity.server.Server;
import org.comroid.mcsd.core.model.ModuleType;
import org.comroid.mcsd.core.model.ServerPropertiesModifier;
import org.comroid.mcsd.core.module.FileModule;
import org.comroid.mcsd.core.module.ServerModule;
import org.comroid.mcsd.core.module.console.ConsoleModule;
import org.comroid.mcsd.core.module.internal.side.agent.RabbitLinkModule;
import org.comroid.mcsd.core.module.internal.side.hub.ConsoleFromRabbitModule;
import org.comroid.mcsd.core.module.internal.side.hub.EventsFromRabbitModule;
import org.comroid.mcsd.core.module.player.PlayerEventModule;
import org.comroid.mcsd.core.module.remote.ssh.SshFileModule;
import org.comroid.mcsd.core.repo.module.ModuleRepo;
import org.comroid.mcsd.core.repo.server.ServerRepo;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static org.comroid.api.func.util.Streams.append;
import static org.comroid.api.func.util.Streams.filter;

@Log
@Service
@DependsOn("migrateEntities")
public class ServerManager {
    public static final Maintenance.Inspection MI_MissingFileModule = Maintenance.Inspection.builder()
            .name("Missing FileModule")
            .description("A module is missing a FileModule")
            .format("%s is missing a FileModule in %s %s")
            .build();
    public static final Duration TickRate = Duration.ofSeconds(30);
    private final Map<UUID, Entry> cache = new ConcurrentHashMap<>();
    private @Autowired ServerRepo servers;
    private @Autowired SidedConfiguration sideConfig;
    private @Autowired ModuleRepo<ModulePrototype> moduleRepo;

    public void startAll(List<Server> servers) {
        servers.stream()
                .map(ThrowingFunction.logging(log, srv -> get(srv.getId()).assertion("Could not initialize " + srv)))
                //.flatMap(filter(Objects::nonNull, $ -> log.severe("A server was not initialized correctly")))
                .map(entry -> {
                    assert entry != null;
                    var c = entry.refreshModules();
                    entry.reloadInternalModules();
                    entry.execute(entry.executor, TickRate);
                    return "%s loaded %d modules".formatted(entry.server, c);
                })
                .forEach(log::info);
    }

    public Wrap<Entry> get(Server server) {
        return get(server.getId());
    }

    public Wrap<Entry> get(final UUID id) {
        if (cache.containsKey(id))
            return () -> cache.get(id);
        var result = servers.findById(id);
        if (result.isEmpty())
            return Wrap.empty();
        final var server = result.get();
        return Wrap.of(cache.computeIfAbsent(id, k -> new Entry(server)));
    }

    public Component tree(Server server) {
        return get(server.getId()).assertion(server + " not initialized");
    }

    @Value
    @Order(99)
    public class Entry extends Component.Base {
        /**
         * the related server
         */
        Server server;
        /**
         * persistent server modules
         */
        Map<ModulePrototype, ServerModule<ModulePrototype>> tree = new ConcurrentHashMap<>();
        /**
         * internal modules
         */
        List<ServerModule<?>> internal = new ArrayList<>();
        /**
         * executor for modules
         */
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(4, new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger(0);

            @Override
            public Thread newThread(@NotNull Runnable r) {
                return new Thread(r, "server_" + server.getId() + "_exec_" + count.incrementAndGet());
            }
        });
        /**
         * nonnull when running; close to {@linkplain #terminate() terminate}
         */
        AtomicReference<UncheckedCloseable> running = new AtomicReference<>();

        public void reloadInternalModules() {
            var helper = new Object() {
                <T extends ServerModule<?>> void loadIfAbsent(Class<T> type, Supplier<T> supplier) {
                    if (component(type).isNull()) {
                        log.info(server + " did not load a "+type.getSimpleName()+"; connecting to rabbit");
                        internal.add(supplier.get());
                    }
                }
            };

            internal.clear();
            // load rabbitmq communication modules
            switch (sideConfig.getSide()) {
                case Agent -> internal.add(new RabbitLinkModule(this));
                case Hub -> {
                    helper.loadIfAbsent(ConsoleModule.class, () -> new ConsoleFromRabbitModule(this));
                    helper.loadIfAbsent(PlayerEventModule.class, () -> new EventsFromRabbitModule(this));
                    noAutoFs:
                    if (component(FileModule.class).isNull()) {
                        var ssh = server.getSsh();
                        if (ssh == null)
                            break noAutoFs;
                        log.info(server + " did not load a FileModule; using SCP/SFTP");
                        internal.add(new SshFileModule(server, new SshFileModulePrototype(ssh)));
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + sideConfig);
            }
        }

        @Override
        @SneakyThrows
        protected void $initialize() {
            if (sideConfig.getSide() != ModuleType.Side.Agent)
                return;
            updateProperties();
        }

        public AlmostComplete<Properties> updateProperties() throws IOException {
            var serverProperties = server.path("server.properties").toAbsolutePath().toString();
            var files = component(FileModule.class).assertion("No file module is specified");
            files.mkDir(serverProperties);
            final var prop = new Properties();
            try (var input = files.readFile(serverProperties)) {
                prop.load(input);
            }

            // generic
            prop.setProperty("server-port", String.valueOf(server.getPort()));
            prop.setProperty("max-players", String.valueOf(server.getMaxPlayers()));
            prop.setProperty("white-list", String.valueOf(server.isWhitelist() || server.isMaintenance()));

            // query
            prop.setProperty("enable-query", String.valueOf(true));
            prop.setProperty("query.port", String.valueOf(server.getQueryPort()));

            // modules
            Streams.of(moduleRepo.findAllByServerId(server.getId()))
                    .flatMap(Streams.cast(ServerPropertiesModifier.class))
                    .forEach(it -> it.modifyServerProperties(prop));

            return new AlmostComplete<>(() -> prop, it -> {
                try (var out = files.writeFile(serverProperties)) {
                    it.store(out, "Managed Server Properties by MCSD");
                }
            });
        }

        @Override
        public Object addChildren(Object @NotNull ... children) {
            for (Object child : children) {
                if (child instanceof ServerModule<?>) {
                    var key = ((ServerModule<?>) child).getProto();
                    tree.put(key, Polyfill.uncheckedCast(child));
                } else super.addChildren(child);
            }
            return this;
        }

        @Override
        public int removeChildren(Object @NotNull ... children) {
            var c = 0;
            for (Object child : children) {
                ModulePrototype key = null;
                if (child instanceof ModulePrototype)
                    key = ((ModulePrototype) child);
                else if (child instanceof ServerModule<?>)
                    key = ((ServerModule<?>) child).getProto();
                if (key != null) {
                    var old = tree.remove(key);
                    old.terminate();
                    c++;
                } else c += super.removeChildren(child);
            }
            return c;
        }

        @Override
        public void clearChildren() {
            super.clearChildren();
            tree.clear();
        }

        @Override
        public Stream<Object> streamOwnChildren() {
            return tree.values().stream()
                    .collect(append(internal))
                    .map(Polyfill::uncheckedCast);
        }

        /**
         * Terminates and removes all modules that are no longer in DB
         */
        public long cleanupModules() {
            var existing = Streams.of(moduleRepo.findAllByServerId(server.getId())).toList();
            var missing = tree.keySet().stream()
                    .filter(not(existing::contains))
                    .toList();
            return missing.stream()
                    .filter(proto -> removeChildren(proto) > 1)
                    .count();
        }

        private Stream<ModulePrototype> streamProtos() {
            return Streams.of(moduleRepo.findAllByServerId(server.getId()));
        }

        /**
         * Reloads all module configurations without restarting anything
         */
        public long reloadProtos() {
            return streamProtos()
                    .filter(tree::containsKey)
                    .peek(proto -> tree.get(proto).setProto(proto))
                    .count();
        }

        /**
         * Loads all modules that are in DB but are not loaded as a module
         */
        @SuppressWarnings("SuspiciousMethodCalls")
        public long refreshModules() {
            final var side = sideConfig.getSide();
            return streamProtos()
                    .filter(not(tree::containsKey))
                    .flatMap(filter(proto -> {
                                var dtype = proto.getDtype();
                                return sideConfig.isHubConnected()
                                                // when connected; check for side preference
                                                ? dtype.getPreferSide().isFlagSet(side.getAsLong())
                                                // when not connected; check for side allowance
                                                : dtype.getAllowedSides().contains(side);
                            },
                            proto -> log.fine("Not loading proto %s (%s) because side configuration denies it (current: %s; preferred: %s)"
                                    .formatted(proto, proto.getClass().getSimpleName(), side.name(), proto.getDtype().getPreferSide()))))
                    .<ServerModule<?>>map(proto -> {
                        log.fine("Loading proto " + proto);
                        var module = proto.toModule(server);
                        module.setParent(this);
                        addChildren(module);
                        return module;
                    })
                    .peek(this::addChildren)
                    .count();
        }

        /**
         * Terminates the entire tree and reloads all modules from scratch
         */
        public long reloadModules() {
            terminate();
            var running = this.running.get();
            if (running != null)
                running.close();
            clearChildren();

            var count = refreshModules();
            running = execute(executor, TickRate);
            this.running.set(running);
            return count;
        }
    }
}
