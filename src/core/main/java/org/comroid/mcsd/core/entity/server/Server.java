package org.comroid.mcsd.core.entity.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.rmmccann.minecraft.status.query.MCQuery;
import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.ClientListener;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.ProtocolState;
import com.github.steveice10.mc.protocol.data.status.ServerStatusInfo;
import com.github.steveice10.mc.protocol.packet.status.clientbound.ClientboundStatusResponsePacket;
import com.github.steveice10.mc.protocol.packet.status.serverbound.ServerboundStatusRequestPacket;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.TcpClientSession;
import io.graversen.minecraft.rcon.Defaults;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import me.dilley.MineStat;
import org.comroid.annotations.Category;
import org.comroid.annotations.Description;
import org.comroid.annotations.Ignore;
import org.comroid.api.attr.IntegerAttribute;
import org.comroid.api.func.ext.Wrap;
import org.comroid.api.func.util.Bitmask;
import org.comroid.api.func.util.Streams;
import org.comroid.api.info.Maintenance;
import org.comroid.api.net.Token;
import org.comroid.mcsd.api.dto.comm.StatusMessage;
import org.comroid.mcsd.api.model.Status;
import org.comroid.mcsd.core.util.Util;
import org.comroid.mcsd.core.MCSD;
import org.comroid.mcsd.core.ServerManager;
import org.comroid.mcsd.core.entity.AbstractEntity;
import org.comroid.mcsd.core.entity.module.FileModulePrototype;
import org.comroid.mcsd.core.entity.module.ModulePrototype;
import org.comroid.mcsd.core.entity.system.Agent;
import org.comroid.mcsd.core.entity.system.DiscordBot;
import org.comroid.mcsd.core.entity.system.ShConnection;
import org.comroid.mcsd.core.model.ModuleType;
import org.comroid.mcsd.core.module.FileModule;
import org.comroid.mcsd.core.module.ServerModule;
import org.comroid.util.MultithreadUtil;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.comroid.mcsd.core.util.ApplicationContextProvider.bean;

@Slf4j
@Getter
@Setter
@Entity
@AllArgsConstructor
@RequiredArgsConstructor
@Category(value = "General", order = -99, desc = @Description("Core Server Configuration"))
public class Server extends AbstractEntity {
    private static final Map<UUID, StatusMessage> statusCache = new ConcurrentHashMap<>();
    public static final Maintenance.Inspection MI_StatusError = Maintenance.Inspection.builder()
            .name("Status Error")
            .description("Unable to fetch Server Status")
            .format("Unable to fetch status of %s")
            .build();
    public static final Duration statusCacheLifetime = Duration.ofMinutes(1);
    public static final Duration statusTimeout = Duration.ofSeconds(30);
    private static final Duration TickRate = Duration.ofMinutes(1);
    private @Nullable String homepage;
    private String mcVersion = "1.19.4";
    private String host;
    private int port = 25565;
    private @Deprecated String directory = "~/minecraft";
    private Mode mode = Mode.Paper;
    private boolean enabled = false;
    private boolean managed = false;
    private boolean whitelist = false;
    private boolean maintenance = false;
    private int maxPlayers = 20;
    private int queryPort = 25565;
    private @ElementCollection(fetch = FetchType.EAGER) List<String> tickerMessages;
    private @Nullable @ManyToOne Agent agent; // todo: make not nullable with db migration
    private @Nullable @ManyToOne ShConnection ssh;
    // cannot remove these because they are needed for migration
    private @Ignore @Deprecated int rConPort = Defaults.RCON_PORT;
    private @Ignore @Deprecated @Getter(onMethod = @__(@JsonIgnore)) String rConPassword = Token.random(16, false);
    private @Ignore @Deprecated @ManyToOne ShConnection shConnection;
    private @Ignore @Deprecated @ManyToOne @Nullable DiscordBot discordBot;
    private @Ignore @Deprecated @Nullable String PublicChannelWebhook;
    private @Ignore @Deprecated @Nullable @Column(unique = true) Long PublicChannelId;
    private @Ignore @Deprecated @Nullable Long ModerationChannelId;
    private @Ignore @Deprecated @Nullable @Column(unique = true) Long ConsoleChannelId;
    private @Ignore @Deprecated @Nullable String ConsoleChannelPrefix;
    private @Ignore @Deprecated long publicChannelEvents = 0xFFFF_FFFF;
    private @Ignore @Deprecated boolean fancyConsole = true;
    private @Ignore @Deprecated boolean forceCustomJar = false;
    private @Ignore @Deprecated @Nullable @Column(columnDefinition = "TEXT") String customCommand = null;
    private @Ignore @Deprecated byte ramGB = 4;
    private @Ignore @Deprecated @Nullable Duration backupPeriod = Duration.ofHours(12);
    private @Ignore @Deprecated Instant lastBackup = Instant.ofEpochMilli(0);
    private @Ignore @Deprecated @Nullable Duration updatePeriod = Duration.ofDays(7);
    private @Ignore @Deprecated Instant lastUpdate = Instant.ofEpochMilli(0);

    public Set<ModuleType<?, ?>> getFreeModuleTypes() {
        var existing = Streams.of(bean(MCSD.class)
                        .getModules()
                        .findAllByServerId(getId()))
                .map(ModulePrototype::getDtype)
                .toList();
        return ModuleType.cache.values()
                .stream()
                .filter(Predicate.not(existing::contains))
                .collect(Collectors.toUnmodifiableSet());
    }

    @JsonIgnore
    public boolean isVanilla() {
        return mode == Mode.Vanilla;
    }

    @JsonIgnore
    public boolean isPaper() {
        return mode == Mode.Paper;
    }

    @JsonIgnore
    public boolean isForge() {
        return mode == Mode.Forge;
    }

    @JsonIgnore
    public boolean isFabric() {
        return mode == Mode.Fabric;
    }

    @Language("sh")
    private String wrapDevNull(@Language("sh") String cmd) {
        return "export TERM='xterm' && script -q /dev/null < <(echo \""+cmd+"\"; cat)";
        //return "export TERM='xterm' && echo \""+cmd+"\" | script /dev/null";
    }

    @Override
    public String toString() {
        return "Server " + getName();
    }

    @Category(value = "Info", order = -10, desc = @Description("Useful Information"))
    public String getDashboardURL() {
        return "https://mc.comroid.org/server/" + getId();
    }

    @Category(value = "Info")
    public String getViewURL() {
        return "https://mc.comroid.org/server/view/" + getId();
    }

    @Category(value = "Info")
    public String getAddress() {
        return host + ":" + port;
    }

    @Category(value = "Info")
    public String getThumbnailURL() {
        return "https://mc-api.net/v3/server/favicon/" + getAddress();
    }

    @Category(value = "Info")
    public String getStatusURL() {
        return "https://mc-api.net/v3/server/ping/" + getAddress();
    }

    @Category(value = "Info")
    public String getJarInfoUrl() {
        var type = switch(mode){
            case Vanilla -> "vanilla";
            case Paper -> "servers";
            case Forge, Fabric -> "modded";
        };
        return "https://serverjars.com/api/fetchDetails/%s/%s/%s".formatted(type,mode.name().toLowerCase(),mcVersion);
    }

    @Category(value = "Info")
    public String getJarUrl() {
        var type = switch(mode){
            case Vanilla -> "vanilla";
            case Paper -> "servers";
            case Forge, Fabric -> "modded";
        };
        return "https://serverjars.com/api/fetchJar/%s/%s/%s".formatted(type,mode.name().toLowerCase(),mcVersion);
    }

    @Category(value = "Info")
    public String getLoaderName() {
        return mode.getName();
    }

    public Path path(String... extra) {
        return Paths.get(component(FileModule.class)
                .map(ServerModule::getProto)
                .<FileModulePrototype>castRef()
                .map(FileModulePrototype::getDirectory)
                .orElse(""), extra);
    }

    @SneakyThrows
    public CompletableFuture<StatusMessage> status() {
        log.trace("Getting status of Server %s".formatted(this));
        final var errors = new ArrayList<Throwable>();
        return CompletableFuture.supplyAsync(() -> Objects.requireNonNull(statusCache.computeIfPresent(getId(), (k, v) -> {
                    if (v.getTimestamp().plus(statusCacheLifetime).isBefore(Instant.now()))
                        return null;
                    return v;
                }), "Status cache outdated"))
                .exceptionally(t ->
                {
                    log.debug("Unable to get server status from cache [" + t.getMessage() + "], using Query...");
                    log.trace("Exception was", t);
                    // do not include this exception as its only about cache status
                    //errors.add(t);

                    try (var query = new MCQuery(host, getQueryPort())) {
                        var stat = query.fullStat();
                        return statusCache.compute(getId(), (id, it) -> it == null ? new StatusMessage(id) : it)
                                //todo
                                //.withRcon(serverConnection.rcon.isConnected() ? Status.Online : Status.Offline)
                                //.withSsh(serverConnection.game.channel.isOpen() ? Status.Online : Status.Offline)
                                .withStatus(isMaintenance() ? Status.in_maintenance_mode : Status.online)
                                .withPlayerCount(stat.getOnlinePlayers())
                                .withPlayerMax(stat.getMaxPlayers())
                                .withMotd(stat.getMOTD())
                                .withGameMode(stat.getGameMode())
                                .withPlayers(stat.getPlayerList())
                                .withWorldName(stat.getMapName());
                    }
                })
                .exceptionallyCompose(t -> {
                    log.debug("Unable to get server status using Query [" + t.getMessage() + "], using MC Protocol...");
                    log.trace("Exception was", t);
                    errors.add(t);

                    final MinecraftProtocol protocol = new MinecraftProtocol();
                    final var session = new TcpClientSession(host, port, protocol);
                    try {
                        return MultithreadUtil.<ServerStatusInfo>asyncFinish(task -> {
                                    session.connect();
                                    session.addListener(new ClientListener(ProtocolState.STATUS) {
                                        @Override
                                        public void packetReceived(Session session, Packet packet) {
                                            if (!(packet instanceof ClientboundStatusResponsePacket csr)) {
                                                super.packetReceived(session, packet);
                                                return;
                                            }
                                            task.accept(csr.getInfo());
                                        }
                                    });
                                    session.send(new ServerboundStatusRequestPacket());
                                }).orTimeout(statusTimeout.toSeconds(), TimeUnit.SECONDS)
                                .thenApply(stat -> {
                                    var players = stat.getPlayerInfo();
                                    return statusCache.compute(getId(), (id, it) -> it == null ? new StatusMessage(id) : it)
                                            //todo
                                            //.withRcon(serverConnection.rcon.isConnected() ? Status.Online : Status.Offline)
                                            //.withSsh(serverConnection.game.channel.isOpen() ? Status.Online : Status.Offline)
                                            .withStatus(isMaintenance() ? Status.in_maintenance_mode : Status.online)
                                            .withMotd(Util.kyoriComponentString(stat.getDescription()))
                                            .withPlayerCount(players.getOnlinePlayers())
                                            .withPlayerMax(players.getMaxPlayers())
                                            .withPlayers(players.getPlayers().stream()
                                                    .map(GameProfile::getName)
                                                    .toList());
                                }).orTimeout(15, TimeUnit.SECONDS);
                    } finally {
                        session.disconnect("goodbye");
                    }
                })
                .exceptionally(t -> {
                    log.debug("Unable to get server status using MC Protocol [" + t.getMessage() + "], using MineStat...");
                    log.trace("Exception was", t);
                    errors.add(t);

                    var stat = new MineStat(host, getPort());
                    return statusCache.compute(getId(), (id, it) -> it == null ? new StatusMessage(id) : it)
                            //todo
                            //.withRcon(serverConnection.rcon.isConnected() ? Status.Online : Status.Offline)
                            //.withSsh(serverConnection.game.channel.isOpen() ? Status.Online : Status.Offline)
                            .withStatus(stat.isServerUp() ? isMaintenance() ? Status.in_maintenance_mode : Status.online : Status.offline)
                            .withPlayerCount(stat.getCurrentPlayers())
                            .withPlayerMax(stat.getMaximumPlayers())
                            .withMotd(Objects.requireNonNullElse(stat.getStrippedMotd(), ""))
                            .withGameMode(stat.getGameMode());
                })
                .orTimeout(30, TimeUnit.SECONDS)
                .exceptionally(t -> {
                    log.warn("Unable to get server status [" + t.getMessage() + "]");
                    log.debug("Exception was", t);
                    errors.add(t);
                    MI_StatusError.new CheckResult(getId(), this, errors.toArray());

                    return statusCache.compute(getId(), (id, it) -> it == null ? new StatusMessage(id) : it)
                            //todo
                            //.withRcon(serverConnection.rcon.isConnected() ? Status.Online : Status.Offline)
                            //.withSsh(serverConnection.game.channel.isOpen() ? Status.Online : Status.Offline);
                            ;
                })
                .thenApply(msg -> {
                    statusCache.put(getId(), msg);
                    return msg;
                });
    }

    public <T extends ServerModule<?>> Wrap<T> component(Class<T> type) {
        return Wrap.ofStream(components(type));
    }
    public <T extends ServerModule<?>> Stream<T> components(Class<T> type) {
        return bean(ServerManager.class).get(getId())
                .assertion(this+" not initialized")
                .components(type);
    }

    public enum Mode implements IntegerAttribute {
        Vanilla, Paper, Forge, Fabric
    }

    @Deprecated
    public enum Permission implements Bitmask.Attribute<Permission> {
        Status, Start, Stop, Console, Backup, Files
    }
    public enum ConsoleMode implements IntegerAttribute { Append, Scroll, ScrollClean }
}
