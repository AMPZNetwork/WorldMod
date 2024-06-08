package com.ampznetwork.worldmod.spigot;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.database.EntityService;
import com.ampznetwork.worldmod.api.model.adp.PlayerAdapter;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.core.WorldModCommands;
import com.ampznetwork.worldmod.core.database.file.LocalEntityService;
import com.ampznetwork.worldmod.core.database.hibernate.HibernateEntityService;
import com.ampznetwork.worldmod.spigot.adp.SpigotEventDispatch;
import com.ampznetwork.worldmod.spigot.adp.SpigotPlayerAdapter;
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
import java.util.logging.Level;

import static org.bukkit.Bukkit.getPluginManager;
import static org.comroid.api.func.util.Debug.isDebug;

@Getter
public class WorldMod$Spigot extends JavaPlugin implements WorldMod {
    private final PlayerAdapter playerAdapter = new SpigotPlayerAdapter(this);
    private final SpigotEventDispatch eventDispatch = new SpigotEventDispatch(this);
    private final Collection<Region> regions = new HashSet<>();
    private final Collection<Group> groups = new HashSet<>();
    private FileConfiguration config;
    private EntityService entityService;
    private Command.Manager cmdr;

    @Override
    public void onLoad() {
        if (!isDebug())
            saveDefaultConfig();
        this.config = super.getConfig();

        WorldModCommands.worldMod = this;

        this.cmdr = new Command.Manager();
        cmdr.new Adapter$Spigot(this);
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
        return cmdr.autoComplete(alias + ' ' + String.join(" ", args),
                        String.valueOf(args.length),
                        args[args.length-1])
                .map(Command.AutoCompletionOption::key)
                .toList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull org.bukkit.command.Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        try {
            cmdr.execute(command.getName() + ' ' + String.join(" ", args),
                    this,
                    sender,
                    command,
                    sender instanceof Player plr ? plr.getUniqueId() : null);
            return true;
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Command " + label + " failed", e);
            return false;
        }
    }

    @Command(ephemeral = true)
    public String reload() {
        onEnable();
        onDisable();
        return "Reload complete!";
    }
}
