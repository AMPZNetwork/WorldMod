package com.ampznetwork.worldmod.spigot;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.WorldModCommands;
import com.ampznetwork.worldmod.api.model.adp.PlayerAdapter;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.spigot.adp.SpigotEventDispatch;
import com.ampznetwork.worldmod.spigot.adp.SpigotPlayerAdapter;
import lombok.Getter;
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
import java.util.Map;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getPluginManager;

@Getter
public class WorldMod$Spigot extends JavaPlugin implements WorldMod {
    private final PlayerAdapter playerAdapter = new SpigotPlayerAdapter(this);
    private final SpigotEventDispatch eventDispatch = new SpigotEventDispatch(this);
    private final Collection<Region> regions = new HashSet<>();
    private final Collection<Group> groups = new HashSet<>();
    private FileConfiguration config;
    private Command.Manager cmdr;

    @Override
    public void onLoad() {
        this.config = getConfig();
        this.cmdr = new Command.Manager();
        cmdr.new Adapter$Spigot(this);
        cmdr.register(new WorldModCommands(this));
        cmdr.register(this);
        cmdr.initialize();
    }

    @Override
    public void onEnable() {
        getPluginManager().registerEvents(eventDispatch, this);
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
                .map(Map.Entry::getKey)
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
