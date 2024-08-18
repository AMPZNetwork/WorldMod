package com.ampznetwork.worldmod.spigot;

import com.ampznetwork.libmod.spigot.SubMod$Spigot;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.core.WorldModCommands;
import com.ampznetwork.worldmod.spigot.adp.internal.SpigotEventDispatch;
import com.ampznetwork.worldmod.spigot.adp.internal.SpigotPlayerAdapter;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.comroid.api.func.util.Command;
import org.comroid.api.java.StackTraceUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.bukkit.Bukkit.*;
import static org.comroid.api.func.util.Debug.*;

@Getter
public class WorldMod$Spigot extends SubMod$Spigot implements WorldMod {
    static {
        StackTraceUtils.EXTRA_FILTER_NAMES.add("com.ampznetwork");
    }

    private final SpigotPlayerAdapter playerAdapter = new SpigotPlayerAdapter(this);
    private final SpigotEventDispatch eventDispatch = new SpigotEventDispatch(this);
    private final Collection<Region> regions = new HashSet<>();
    private final Collection<Group>  groups  = new HashSet<>();
    private       FileConfiguration  config;

    public WorldMod$Spigot() {
        super(Set.of(Capability.Database), Set.of(Region.class, Group.class));
    }

    @Override
    public void onLoad() {
        cmdr.register(WorldModCommands.class);
        cmdr.register(this);

        super.onLoad();

        if (!isDebug())
            saveDefaultConfig();
        this.config = super.getConfig();
    }

    @Override
    @SneakyThrows
    public void onEnable() {
        getPluginManager().registerEvents(eventDispatch, this);
    }

    @Override
    @SneakyThrows
    public void onDisable() {
        this.entityService.terminate();
    }

    @Nullable
    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull org.bukkit.command.Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        return adapter.onTabComplete(sender, command, alias, args);
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull org.bukkit.command.Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        return adapter.onCommand(sender, command, label, args);
    }

    @Command(ephemeral = true)
    public String reload() {
        onDisable();
        onEnable();
        return "Reload complete!";
    }
}
