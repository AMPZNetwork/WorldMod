package com.ampznetwork.worldmod.spigot;

import com.ampznetwork.libmod.api.interop.database.IEntityService;
import com.ampznetwork.libmod.api.model.info.DatabaseInfo;
import com.ampznetwork.libmod.spigot.SubMod$Spigot;
import com.ampznetwork.worldmod.api.model.TextResourceProvider;
import com.ampznetwork.worldmod.api.model.WandType;
import com.ampznetwork.worldmod.api.model.log.LogEntry;
import com.ampznetwork.worldmod.api.model.query.IQueryManager;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.api.model.sel.Area;
import com.ampznetwork.worldmod.core.WorldMod$Core;
import com.ampznetwork.worldmod.core.WorldModCommands;
import com.ampznetwork.worldmod.core.query.QueryManager;
import com.ampznetwork.worldmod.spigot.adp.internal.SpigotEventDispatch;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.WorldInfo;
import org.comroid.api.func.util.Command;
import org.comroid.api.func.util.Streams;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bukkit.Bukkit.*;

@Slf4j
@Getter
public class WorldMod$Spigot extends SubMod$Spigot implements WorldMod$Core {
    private final SpigotEventDispatch        eventDispatch = new SpigotEventDispatch(this);
    private       FileConfiguration          config;
    private       Map<String, IQueryManager> queryManagers;

    public WorldMod$Spigot() {
        super(Set.of(Capability.Database), Set.of(Region.class, Group.class, LogEntry.class, Area.class));
    }

    @Override
    public boolean isSafeMode() {
        return getConfig().getBoolean("safe-mode", true);
    }

    @Override
    public boolean chunkloadWhileOnlineOnly() {
        return config.getBoolean("chunkload-while-online-only", true);
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
    public DatabaseInfo database() {
        var obj = config.getConfigurationSection("database");
        if (obj == null) return null;
        return new DatabaseInfo(
                IEntityService.DatabaseType.valueOf(obj.getString("type")),
                obj.getString("url"),
                obj.getString("username"),
                obj.getString("password"));
    }

    @Override
    public @Nullable DatabaseInfo getDatabaseInfo() {
        return config.contains("database") ? database() : null;
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
        loadUnion();

        this.config = super.getConfig();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (queryManagers != null) queryManagers.clear();
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

    public void reloadQueryManagers() {
        this.queryManagers = getWorlds().stream()
                .map(WorldInfo::getName)
                .map(world -> new QueryManager(this, world))
                .collect(Streams.append(new QueryManager(this, Region.GLOBAL_REGION_NAME)))
                .collect(Collectors.toMap(mgr -> mgr.getWorldCondition().getWorlds()[0], Function.identity()));
        getLogger().log(Level.INFO, "Loaded %d query managers".formatted(queryManagers.size()));
    }
}
