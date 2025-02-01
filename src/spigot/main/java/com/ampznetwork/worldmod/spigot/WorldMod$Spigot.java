package com.ampznetwork.worldmod.spigot;

import com.ampznetwork.libmod.spigot.SubMod$Spigot;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.TextResourceProvider;
import com.ampznetwork.worldmod.api.model.WandType;
import com.ampznetwork.worldmod.api.model.log.LogEntry;
import com.ampznetwork.worldmod.api.model.query.IQueryManager;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.api.model.sel.Area;
import com.ampznetwork.worldmod.core.WorldModCommands;
import com.ampznetwork.worldmod.core.query.QueryManager;
import com.ampznetwork.worldmod.spigot.adp.internal.SpigotEventDispatch;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.WorldInfo;
import org.comroid.api.func.util.Command;
import org.comroid.api.java.ResourceLoader;
import org.comroid.api.java.StackTraceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bukkit.Bukkit.*;

@Getter
public class WorldMod$Spigot extends SubMod$Spigot implements WorldMod {
    static {
        StackTraceUtils.EXTRA_FILTER_NAMES.add("com.ampznetwork");
    }

    private final SpigotEventDispatch        eventDispatch = new SpigotEventDispatch(this);
    private       FileConfiguration          config;
    private       Map<String, IQueryManager> queryManagers;

    public WorldMod$Spigot() {
        super(Set.of(Capability.Database), Set.of(Region.class, Group.class, LogEntry.class, Area.class));
    }

    @Override
    @SneakyThrows
    public Properties getMessages() {
        var path = "plugins/WorldMod/messages.lang";
        ResourceLoader.assertFile(WorldModCommands.class, "messages.lang", new File(path), () -> "# Include custom messages here");
        var prop = new Properties();
        try (var fis = new FileInputStream(path)) {
            prop.load(fis);
        }
        return prop;
    }

    @Override
    public boolean loggingSkipsNonPlayer() {
        return config.getBoolean("logging.skip-non-player", true);
    }

    @Override
    public Stream<String> loggingSkipFlagNames() {
        return config.getStringList("logging.skip").stream();
    }

    @Override
    public Map<WandType, String> wandItems() {
        var map = new ConcurrentHashMap<WandType, String>();
        for (var type : WandType.values()) {
            var itemResourceKey = type.defaultItem;
            var str             = config.getString(type.configPath);
            if (str != null) {
                var material = Material.matchMaterial(str);
                if (material != null) itemResourceKey = material.getKey().toString();
            }
            map.put(type, itemResourceKey);
        }
        return map;
    }

    @Override
    public TextResourceProvider text() {
        return new TextResourceProvider(this);
    }

    @Override
    public void onLoad() {
        cmdr.register(WorldModCommands.class);
        cmdr.register(this);

        super.onLoad();

        this.config = super.getConfig();
        reloadQueryManagers();
    }

    @Override
    @SneakyThrows
    public void onEnable() {
        super.onEnable();

        getPluginManager().registerEvents(eventDispatch, this);
    }

    @Command(privacy = Command.PrivacyLevel.PRIVATE)
    public String reload() {
        onDisable();
        reloadConfig();
        reloadQueryManagers();
        onEnable();
        return "Reload complete!";
    }

    private void reloadQueryManagers() {
        queryManagers.clear();
        this.queryManagers = getWorlds().stream()
                .map(WorldInfo::getName)
                .map(mod -> QueryManager.init(this, mod))
                .collect(Collectors.toMap(QueryManager::getWorldName, Function.identity()));
    }
}
