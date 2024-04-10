package org.comroid.mcsd.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.cj.jdbc.Driver;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.ClientBuilder;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.comroid.api.Polyfill;
import org.comroid.api.func.util.Debug;
import org.comroid.api.func.util.DelegateStream;
import org.comroid.api.func.util.Streams;
import org.comroid.api.io.FileHandle;
import org.comroid.api.net.REST;
import org.comroid.api.net.Rabbit;
import org.comroid.api.os.OS;
import org.comroid.mcsd.api.dto.config.McsdConfig;
import org.comroid.mcsd.core.entity.AbstractEntity;
import org.comroid.mcsd.core.entity.module.ModulePrototype;
import org.comroid.mcsd.core.entity.module.console.McsdCommandModulePrototype;
import org.comroid.mcsd.core.entity.module.discord.DiscordModulePrototype;
import org.comroid.mcsd.core.entity.module.local.LocalExecutionModulePrototype;
import org.comroid.mcsd.core.entity.module.local.LocalFileModulePrototype;
import org.comroid.mcsd.core.entity.module.local.LocalShellModulePrototype;
import org.comroid.mcsd.core.entity.module.player.ConsolePlayerEventModulePrototype;
import org.comroid.mcsd.core.entity.module.player.ForceOpModulePrototype;
import org.comroid.mcsd.core.entity.module.player.PlayerListModulePrototype;
import org.comroid.mcsd.core.entity.module.remote.rcon.RconModulePrototype;
import org.comroid.mcsd.core.entity.module.remote.ssh.SshFileModulePrototype;
import org.comroid.mcsd.core.entity.module.status.BackupModulePrototype;
import org.comroid.mcsd.core.entity.module.status.StatusModulePrototype;
import org.comroid.mcsd.core.entity.module.status.UpdateModulePrototype;
import org.comroid.mcsd.core.entity.module.status.UptimeModulePrototype;
import org.comroid.mcsd.core.entity.server.Server;
import org.comroid.mcsd.core.entity.system.Agent;
import org.comroid.mcsd.core.entity.system.DiscordBot;
import org.comroid.mcsd.core.entity.system.ShConnection;
import org.comroid.mcsd.core.entity.system.User;
import org.comroid.mcsd.core.exception.BadRequestException;
import org.comroid.mcsd.core.exception.EntityNotFoundException;
import org.comroid.mcsd.core.model.ModuleType;
import org.comroid.mcsd.core.module.discord.DiscordAdapter;
import org.comroid.mcsd.core.repo.module.ModuleRepo;
import org.comroid.mcsd.core.repo.server.ServerRepo;
import org.comroid.mcsd.core.repo.system.AgentRepo;
import org.comroid.mcsd.core.repo.system.DiscordBotRepo;
import org.comroid.mcsd.core.repo.system.ShRepo;
import org.comroid.mcsd.core.repo.system.UserRepo;
import org.comroid.mcsd.core.util.ApplicationContextProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Configuration
@ImportResource({"classpath:baseBeans.xml"})
@ComponentScan(basePackages = "org.comroid.mcsd.core")
@EntityScan(basePackages = "org.comroid.mcsd.core.entity")
@EnableJpaRepositories(basePackages = "org.comroid.mcsd.core.repo")
public class MCSD {
    @Lazy @Autowired private DefaultListableBeanFactory beanRegistry;
    @Lazy @Autowired private EntityManager entityManager;;
    @Lazy @Autowired private UserRepo users;
    @Lazy @Autowired private ServerRepo servers;
    @Lazy @Autowired private AgentRepo agents;
    @Lazy @Autowired private ShRepo shRepo;
    @Lazy @Autowired private DiscordBotRepo discordBotRepo;
    @Lazy @Autowired private ModuleRepo<ModulePrototype> modules;
    @Lazy @Autowired private ModuleRepo<McsdCommandModulePrototype> modules_mcsd;
    @Lazy @Autowired private ModuleRepo<DiscordModulePrototype> modules_discord;
    @Lazy @Autowired private ModuleRepo<LocalExecutionModulePrototype> modules_localExecution;
    @Lazy @Autowired private ModuleRepo<LocalFileModulePrototype> modules_localFiles;
    @Lazy @Autowired private ModuleRepo<LocalShellModulePrototype> modules_localShell;
    @Lazy @Autowired private ModuleRepo<RconModulePrototype> modules_rcon;
    @Lazy @Autowired private ModuleRepo<SshFileModulePrototype> modules_sshFile;
    @Lazy @Autowired private ModuleRepo<ConsolePlayerEventModulePrototype> modules_consolePlayerEvents;
    @Lazy @Autowired private ModuleRepo<PlayerListModulePrototype> modules_playerList;
    @Lazy @Autowired private ModuleRepo<ForceOpModulePrototype> modules_forceOp;
    @Lazy @Autowired private ModuleRepo<BackupModulePrototype> modules_backup;
    @Lazy @Autowired private ModuleRepo<StatusModulePrototype> modules_status;
    @Lazy @Autowired private ModuleRepo<UpdateModulePrototype> modules_update;
    @Lazy @Autowired private ModuleRepo<UptimeModulePrototype> modules_uptime;

    @Bean(name = "configDir")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnExpression(value = "environment.containsProperty('DEBUG')")
    public FileHandle configDir_Debug() {
        log.info("Using debug configuration directory");
        return new FileHandle("/srv/mcsd-dev/", true);
    }

    @Bean
    @Order
    @ConditionalOnMissingBean(name = "configDir")
    public FileHandle configDir() {
        log.info("Using production configuration directory");
        return new FileHandle("/srv/mcsd/", true);
    }

    @Bean
    public McsdConfig config(@Autowired ObjectMapper objectMapper, @Autowired FileHandle configDir) throws IOException {
        return objectMapper.readValue(configDir.createSubFile("config.json"), McsdConfig.class);
    }

    @Bean
    @Nullable
    public DiscordAdapter bot(@Autowired McsdConfig config) {
        return config.getDiscordToken() != null ? new DiscordAdapter(new DiscordBot()
                .setToken(config.getDiscordToken())
                .setShardCount(1)) : null;
    }

    @Bean
    public DataSource dataSource(@Autowired McsdConfig config) {
        var db = config.getDatabase();
        return DataSourceBuilder.create()
                .driverClassName(Driver.class.getCanonicalName())
                .url(db.getUrl())
                .username(db.getUsername())
                .password(db.getPassword())
                .build();
    }

    @Bean
    @Nullable
    public Rabbit rabbit(@Autowired McsdConfig config) {
        return Rabbit.of(config.getRabbitUri()).get();
    }

    @Bean
    public ScheduledExecutorService scheduler() {
        return Executors.newScheduledThreadPool(32);
    }

    @Bean
    public OS.Host hostname() {
        return OS.current.getPrimaryHost();
    }

    @Bean
    public SshClient ssh() {
        SshClient client = ClientBuilder.builder()
                .serverKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE) // todo This is bad and unsafe
                .build();
        client.start();
        return client;
    }

    @Bean
    @Lazy(false)
    public ScheduledFuture<?> shutdownForAutoUpdateTask(@Autowired ScheduledExecutorService scheduler) {
        return scheduler.scheduleAtFixedRate(()->{
            try {
                var info = REST.get("https://api.github.com/repos/comroid-git/mc-server-hub/commits/main?per_page=1")
                        .join().getBody();
                var recent = info.get("sha").asString();
                var current = DelegateStream.readAll(ClassLoader.getSystemResourceAsStream("commit.txt"));
                if (!current.equals(recent)) {
                    log.info("Shutting down for auto update");
                    System.exit(0);
                }
            } catch (Throwable t) {
                log.error("Unable to fetch latest commit", t);
            }
        }, 72, 72, TimeUnit.HOURS);
    }

    @Bean
    @Lazy(false)
    @Transactional
    @SuppressWarnings("deprecation")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @DependsOn("applicationContextProvider")
    public Set<AbstractEntity> migrateEntities() {
        return Set.of();
        /*
        class Helper {
            <T extends AbstractEntity> void getOrMigrate(Server server, ModuleType<?, ?> type, Supplier<T> migratedObj) {
                var repo = type.getObtainRepo().apply(MCSD.this);
                if (repo.findByServerIdAndDtype(server.getId(), type).isPresent())
                    return;
                var migrate = migratedObj.get();
                save(repo, migrate);
                repo.save(Polyfill.uncheckedCast(migrate));
            }

            <T extends AbstractEntity> void save(AbstractEntity.Repo<?> repo, T migrate) {
            }
        }

        var helper = new Helper();
        var yield = new HashSet<AbstractEntity>();

        log.info("Checking if DB needs migration");

        var invalid = Streams.of(users.findAll())
                .filter(usr -> usr.getDiscordId() == null && usr.getMinecraftId() == null && usr.getHubId() == null)
                .toList();
        if (!invalid.isEmpty()) {
            log.info("Deleting " + invalid.size() + " invalid Users that have no IDs");
            users.deleteAll(invalid);
        }

        // migrate server.agent fields
        servers.saveAll(entityManager.createQuery("SELECT s FROM Server s WHERE s.agent = null", Server.class)
                .getResultList().stream()
                .peek(srv -> Optional.ofNullable(srv.getAgent()).ifPresentOrElse(
                        srv::setAgent,
                        () -> log.warn("Could not migrate " + srv + "s Agent ID. Please set Agent ID manually (Server ID: " + srv.getId() + ")")
                ))
                .peek(yield::add)
                .toList());

        // migrate servers to use modules
        Streams.of(servers.findMigrationCandidates(0))
                .peek(server -> {
                    //todo: complete migration code
                    //    StatusModule.Factory,
                    helper.getOrMigrate(server, ModuleType.Status,
                            () -> new StatusModulePrototype()
                                    .setServer(server));
                    //    LocalFileModule.Factory,
                    helper.getOrMigrate(server, ModuleType.LocalFile,
                            () -> new LocalFileModulePrototype()
                                    .setDirectory(server.getDirectory())
                                    .setBackupsDir(server.getShConnection().getBackupsDir())
                                    .setForceCustomJar(server.isForceCustomJar())
                                    .setServer(server));
                    //    UptimeModule.Factory,
                    helper.getOrMigrate(server, ModuleType.Uptime,
                            () -> new UptimeModulePrototype()
                                    .setServer(server));
                    //    LocalExecutionModule.Factory,
                    helper.getOrMigrate(server, ModuleType.LocalExecution,
                            () -> new LocalExecutionModulePrototype()
                                    .setRamGB(server.getRamGB())
                                    .setCustomCommand(server.getCustomCommand())
                                    .setServer(server));
                    //    //todo: fix BackupModule.Factory,
                    helper.getOrMigrate(server, ModuleType.Backup,
                            () -> new BackupModulePrototype()
                                    .setEnabled(false)
                                    .setServer(server));
                    //    ConsolePlayerEventModule.Factory,
                    helper.getOrMigrate(server, ModuleType.ConsolePlayerEvent,
                            () -> new ConsolePlayerEventModulePrototype()
                                    .setServer(server));
                    //    DiscordModule.Factory
                    helper.getOrMigrate(server, ModuleType.Discord,
                            () -> new DiscordModulePrototype()
                                    .setDiscordBot(server.getDiscordBot())
                                    .setPublicChannelId(server.getPublicChannelId())
                                    .setPublicChannelEvents(server.getPublicChannelEvents())
                                    .setModerationChannelId(server.getModerationChannelId())
                                    .setConsoleChannelId(server.getConsoleChannelId())
                                    .setConsoleChannelPrefix(server.getConsoleChannelPrefix())
                                    .setFancyConsole(server.isFancyConsole())
                                    .setServer(server));

                    server.setVersion(AbstractEntity.CurrentVersion);
                })
                .peek(servers::save)
                .forEach(yield::add);

        if (!yield.isEmpty())
            log.info("Migrated entities:" + yield.stream()
                    .map(Objects::toString)
                    .collect(Collectors.joining("\n\t- ", "\n\t- ", "")));

        // early init for better load times
        log.info(ModuleType.cache.size() + " Module types loaded");

        return yield;
         */
    }

    public static String wrapHostname(String hostname) {
        return "http%s://%s%s".formatted(Debug.isDebug() ? "" : "s", hostname, Debug.isDebug() ? ":42064" : "");
    }

    public <E extends AbstractEntity> Class<E> findType(String type) {
        return Polyfill.uncheckedCast(switch (type) {
            case "agent" -> Agent.class;
            case "discordBot" -> DiscordBot.class;
            case "server" -> Server.class;
            case "sh" -> ShConnection.class;
            case "user" -> User.class;
            default -> throw new BadRequestException("unknown type: " + type);
        });
    }

    public <E extends AbstractEntity> AbstractEntity.Repo<E> findRepository(String type) {
        return Polyfill.uncheckedCast(switch (type) {
            case "agent" -> agents;
            case "discordBot" -> discordBotRepo;
            case "server" -> servers;
            case "sh" -> shRepo;
            case "user" -> users;
            default -> throw new BadRequestException("unknown type: " + type);
        });
    }

    public AbstractEntity findEntity(String type, UUID id) {
        return findRepository(type).findById(id).orElseThrow(()->new EntityNotFoundException(findType(type),id));
    }

    @ApiStatus.Internal
    public List<Server> servers() {
        return ApplicationContextProvider.<List<?>, List<Server>>bean(List.class, "servers");
    }
}

