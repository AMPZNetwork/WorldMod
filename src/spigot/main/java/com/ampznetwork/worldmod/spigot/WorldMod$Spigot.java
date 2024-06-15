package com.ampznetwork.worldmod.spigot;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.database.EntityService;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.core.WorldModCommands;
import com.ampznetwork.worldmod.core.database.file.LocalEntityService;
import com.ampznetwork.worldmod.core.database.hibernate.HibernateEntityService;
import com.ampznetwork.worldmod.spigot.adp.internal.SpigotEventDispatch;
import com.ampznetwork.worldmod.spigot.adp.internal.SpigotPlayerAdapter;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.comroid.api.func.util.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.bukkit.Bukkit.getPluginManager;
import static org.comroid.api.func.util.Debug.isDebug;
import static org.comroid.api.func.util.Streams.append;

@Getter
public class WorldMod$Spigot extends JavaPlugin implements WorldMod {
    private final SpigotPlayerAdapter playerAdapter = new SpigotPlayerAdapter(this);
    private final SpigotEventDispatch eventDispatch = new SpigotEventDispatch(this);
    private final Collection<Region> regions = new HashSet<>();
    private final Collection<Group> groups = new HashSet<>();
    private FileConfiguration config;
    private EntityService entityService;
    private Command.Manager cmdr;
    private Command.Manager.Adapter$Spigot adapter;

    @Override
    public void onLoad() {
        if (!isDebug())
            saveDefaultConfig();
        this.config = super.getConfig();

        this.cmdr = new Command.Manager();
        this.adapter = cmdr.new Adapter$Spigot(this) {
            @Override
            protected Stream<Object> collectExtraArgs(@NotNull CommandSender sender) {
                if (!(sender instanceof Player player))
                    throw new Command.Error("Cannot be used from console");
                var pos = playerAdapter.getPosition(player.getUniqueId());
                return super.collectExtraArgs(sender)
                        .collect(append(WorldMod$Spigot.this, entityService.findRegion(pos,
                                playerAdapter.getWorldName(player.getUniqueId())).orElse(null)));
            }
        };
        cmdr.register(WorldModCommands.class);
        cmdr.register(this);
        cmdr.initialize();
    }

    @Override
    @SneakyThrows
    public void onEnable() {
        var dbImpl = config.getString("worldmod.entity-service", "database");
        var dbType = EntityService.DatabaseType.valueOf(config.getString("worldmod.database.type", "h2"));
        var dbUrl = config.getString("worldmod.database.url", "jdbc:h2:file:./worldmod.h2");
        var dbUser = config.getString("worldmod.database.username", "sa");
        var dbPass = config.getString("worldmod.database.password", "");
        this.entityService = switch (dbImpl.toLowerCase()) {
            case "file", "local" -> new LocalEntityService(this);
            case "hibernate", "database" -> new HibernateEntityService(this, dbType, dbUrl, dbUser, dbPass);
            default -> throw new IllegalStateException("Unexpected value: " + dbImpl.toLowerCase());
        };

        getPluginManager().registerEvents(eventDispatch, this);
    }

    @Override
    @SneakyThrows
    public void onDisable() {
        this.entityService.terminate();
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull org.bukkit.command.Command command,
                                      @NotNull String alias,
                                      @NotNull String[] args) {
        return adapter.onTabComplete(sender, command, alias, args);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull org.bukkit.command.Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        return adapter.onCommand(sender, command, label, args);
    }

    @Command(ephemeral = true)
    public String reload() {
        onDisable();
        onEnable();
        return "Reload complete!";
    }
}
