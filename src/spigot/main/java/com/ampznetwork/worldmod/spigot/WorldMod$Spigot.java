package com.ampznetwork.worldmod.spigot;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.WorldModCommands;
import com.ampznetwork.worldmod.api.model.adp.PlayerAdapter;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.spigot.adp.SpigotEventDispatch;
import com.ampznetwork.worldmod.spigot.adp.SpigotPlayerAdapter;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.comroid.api.func.util.Command;

import java.util.Collection;
import java.util.HashSet;

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
        cmdr.register(new WorldModCommands(this));
        cmdr.register(this);
        cmdr.new Adapter$Spigot(this);
        cmdr.initialize();
    }

    @Override
    public void onEnable() {
        getPluginManager().registerEvents(eventDispatch, this);
    }
}
