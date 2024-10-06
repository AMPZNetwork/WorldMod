package com.ampznetwork.worldmod.spigot;

import com.ampznetwork.libmod.spigot.SubMod$Spigot;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.log.LogEntry;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.core.WorldModCommands;
import com.ampznetwork.worldmod.spigot.adp.internal.SpigotEventDispatch;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.FileConfiguration;
import org.comroid.api.func.util.Command;
import org.comroid.api.java.StackTraceUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.bukkit.Bukkit.*;

@Getter
public class WorldMod$Spigot extends SubMod$Spigot implements WorldMod {
    static {
        StackTraceUtils.EXTRA_FILTER_NAMES.add("com.ampznetwork");
    }

    private final SpigotEventDispatch eventDispatch = new SpigotEventDispatch(this);
    private       FileConfiguration  config;

    public WorldMod$Spigot() {
        super(Set.of(Capability.Database), Set.of(Region.class, Group.class, LogEntry.class));
    }

    @Override
    public void onLoad() {
        cmdr.register(WorldModCommands.class);
        cmdr.register(this);

        super.onLoad();

        this.config = super.getConfig();
    }

    @Override
    @SneakyThrows
    public void onEnable() {
        super.onEnable();

        getPluginManager().registerEvents(eventDispatch, this);
    }

    @Command(ephemeral = true)
    public String reload() {
        onDisable();
        onEnable();
        return "Reload complete!";
    }

    @Override
    public boolean loggingSkipsNonPlayer() {
        return config.getBoolean("logging.skip-non-player", true);
    }

    @Override
    public Stream<String> loggingSkipFlagNames() {
        return config.getStringList("logging.skip").stream();
    }
}
