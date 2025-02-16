package com.ampznetwork.worldmod.spigot;

import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.spigot.SubMod$Spigot;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
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
import org.comroid.api.func.util.Streams;
import org.comroid.api.java.ResourceLoader;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.bukkit.Bukkit.*;

@Getter
public class WorldMod$Spigot extends SubMod$Spigot implements WorldMod {
    private static final Map<UUID, Map<String, @NotNull Long>> flagLog = new ConcurrentHashMap<>();

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
    public Stream<String> flagNames() {
        return Flag.VALUES.values().stream().flatMap(WorldMod$Spigot::ownAndChildFlagNames);
    }

    @Override
    public Map<String, Long> flagLog(@Nullable Player player) {
        return flagLog.computeIfAbsent(player == null ? new UUID(0, 0) : player.getId(), $ -> {
            @Language("SQL") String fq, q = "select e.* from dev.worldmod_world_log e where e.action = :canonical";
            if (player != null) q += " and e.player_id = :player_id";
            fq = q;
            return Flag.VALUES.values()
                    .stream()
                    .parallel()
                    .map(Flag::getCanonicalName)
                    .collect(Collectors.toConcurrentMap(Function.identity(),
                            canonical -> getEntityService().getAccessor(LogEntry.TYPE)
                                    .querySelect(fq, player == null ? Map.of("canonical", canonical) : Map.of("canonical", canonical, "player_id",
                                            player.getId().toString()))
                                    .count(), Long::sum, ConcurrentHashMap::new));
        });
    }

    @Override
    public boolean isSafeMode() {
        return getConfig().getBoolean("safe-mode", true);
    }

    @Override
    public void onLoad() {
        cmdr.register(WorldModCommands.class);
        cmdr.register(this);

        super.onLoad();

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
                .collect(Collectors.toMap(QueryManager::getWorldName, Function.identity()));
        getLogger().log(Level.INFO, "Loaded %d query managers".formatted(queryManagers.size()));
    }

    private static Stream<String> ownAndChildFlagNames(Flag flag) {
        var name = flag.getName();
        return Stream.concat(Stream.of(name), flag.getChildren().stream().flatMap(WorldMod$Spigot::ownAndChildFlagNames).map(str -> name + '.' + str));
    }
}
