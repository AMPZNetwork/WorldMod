package org.comroid.mcsd.spigot;

import lombok.Getter;
import org.apache.logging.log4j.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.comroid.api.func.ext.Wrap;
import org.comroid.api.func.util.Command;
import org.comroid.api.net.REST;
import org.comroid.api.net.Rabbit;
import org.comroid.mcsd.api.dto.comm.ConsoleData;
import org.comroid.mcsd.api.dto.comm.PlayerEvent;
import org.comroid.mcsd.api.log.RabbitConsoleAppender;
import org.comroid.util.PathUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.bukkit.Bukkit.getConsoleSender;

@Getter
public final class MCSD_Spigot extends JavaPlugin {
    public static final String Unspecified = "<please specify>";
    public static final String DefaultHubBaseUrl = "https://mc.comroid.org";
    public static final String DefaultRabbitUri = "amqp://anonymous:anonymous@rabbitmq.comroid.org:5672/mcsd";
    public static final String DefaultConsoleLevel = Level.INFO.name();
    public static final String ExchangeConsole = "mcsd.module.console";
    public static final String ExchangePlayerEvent = "mcsd.module.player";
    public static final String RouteConsoleInputBase = "input.%s";
    public static final String RouteConsoleOutputBase = "output.%s";
    public static final String RoutePlayerEventBase = "event.%s.%s";

    EventManager eventManager;
    FileConfiguration config;
    Command.Manager cmdr;
    UUID serverId;
    Level consoleLevel;
    RabbitConsoleAppender appender;
    Rabbit.Exchange console;
    Rabbit.Exchange players;

    @Override
    public void onLoad() {
        // config
        this.config = super.getConfig();
        initConfigDefaults();

        // plugin commands
        this.cmdr = new Command.Manager();
        cmdr.new Adapter$Spigot(this);
        cmdr.register(this);
        cmdr.initialize();
    }

    @Override
    public void onEnable() {
        // convert config
        this.serverId = UUID.fromString(Objects.requireNonNull(config.getString("mcsd.server.id"), "Server ID not configured"));
        this.consoleLevel = Level.valueOf(config.getString("mcsd.consoleLevel", DefaultConsoleLevel));

        // rabbitmq
        var rabbit = Wrap.of(config.get("mcsd.rabbitMqUri", DefaultRabbitUri))
                .map(String::valueOf)
                .flatMap(Rabbit::of)
                .requireNonNull("Unable to initialize RabbitMQ connection");
        this.console = rabbit.exchange(ExchangeConsole);
        this.players = rabbit.exchange(ExchangePlayerEvent);

        // rabbit -> console dispatch
        console.route(RouteConsoleInputBase.formatted(serverId), ConsoleData.class)
                .filterData(cData -> cData.getType() == ConsoleData.Type.input)
                .mapData(ConsoleData::getData)
                .subscribeData(cmd -> Bukkit.getScheduler().runTask(this,
                        () -> Bukkit.getServer().dispatchCommand(getConsoleSender(), cmd)));

        // logger configuration
        this.appender = new RabbitConsoleAppender(
                rabbit.exchange(ExchangeConsole)
                        .route(RouteConsoleOutputBase.formatted(serverId), ConsoleData.class),
                consoleLevel);
        appender.start();
        if (!appender.isStarted())
            getLogger().warning("Could not start RabbitAppender");

        // bukkit events
        this.eventManager = new EventManager(this);
        getServer().getPluginManager().registerEvents(eventManager, this);
    }

    @Override
    public void onDisable() {
        this.eventManager.close();
        this.appender.stop();
    }

    public void forward(PlayerEvent event) {
        var routingKey = RoutePlayerEventBase.formatted(
                event.getType().name().toLowerCase(),
                serverId);
        players.route(routingKey, PlayerEvent.class)
                .send(event);
    }

    @Command(permission = "mcsd.reload")
    public String reload() {
        onDisable();
        onEnable();
        return "Reloaded!";
    }

    private String hub(String path) {
        path = PathUtil.sanitize(path);
        if (!path.startsWith("/"))
            path = '/' + path;
        return config.getString("mcsd.hubBaseUrl") + "/api/open" + path;
    }

    private REST.Request req(REST.Method method, String path) {
        return REST.request(method, hub(path))
                .addHeader("Authorization", config.getString("mcsd.agent.token"));
    }

    private void initConfigAttribute(String path, @Nullable String defaultValue, String hint) {
        config.addDefault(path, Objects.requireNonNullElse(defaultValue, Unspecified));
        if (!config.contains(path))
            config.setComments(path, List.of(hint));
    }

    private void initConfigDefaults() {
        initConfigAttribute("mcsd.hubBaseUrl", DefaultHubBaseUrl, "MCSD Hub Base URL");
        initConfigAttribute("mcsd.rabbitMqUri", DefaultRabbitUri, "MCSD RabbitMQ URI");
        initConfigAttribute("mcsd.consoleLevel", DefaultConsoleLevel, "Console Minimum Log Level");
        initConfigAttribute("mcsd.server.id", null, "MCSD Server ID");

        saveConfig();
    }
}
