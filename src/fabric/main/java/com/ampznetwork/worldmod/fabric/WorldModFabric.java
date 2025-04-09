package com.ampznetwork.worldmod.fabric;

import com.ampznetwork.libmod.fabric.SubMod$Fabric;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.TextResourceProvider;
import com.ampznetwork.worldmod.api.model.WandType;
import com.ampznetwork.worldmod.api.model.log.LogEntry;
import com.ampznetwork.worldmod.api.model.query.IQueryManager;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.core.WorldMod$Core;
import com.ampznetwork.worldmod.core.WorldModCommands;
import com.ampznetwork.worldmod.core.query.QueryManager;
import com.ampznetwork.worldmod.fabric.adp.internal.FabricEventDispatch;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.comroid.api.config.ConfigurationManager;
import org.comroid.api.data.seri.MimeType;
import org.comroid.api.func.ext.Context;
import org.comroid.api.func.util.Streams;
import org.comroid.api.java.StackTraceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class WorldModFabric extends SubMod$Fabric implements ModInitializer, WorldMod$Core {
    public static final Logger LOGGER = LoggerFactory.getLogger(WorldMod.AddonName);

    static {
        StackTraceUtils.EXTRA_FILTER_NAMES.add("com.ampznetwork");
    }

    private final FabricEventDispatch                  eventDispatch = new FabricEventDispatch(this);
    private final Collection<Region>                   regions       = new HashSet<>();
    private final Collection<Group>                    groups        = new HashSet<>();
    private final ConfigurationManager<WorldModConfig> configManager = new ConfigurationManager<>(Context.root(),
            WorldModConfig.class,
            "./config/worldmod.json",
            MimeType.JSON);
    private Map<String, IQueryManager> queryManagers;
    private       MinecraftServer                      server;

    public WorldModFabric() {
        super(Set.of(Capability.Database), Set.of(Region.class, Group.class, LogEntry.class));
        queryManagers = Map.of();
    }

    @Override
    public Map<String, IQueryManager> getQueryManagers() {
        return Map.of();
    }

    @Override
    public TextResourceProvider text() {
        return null;
    }

    @Override
    public boolean isSafeMode() {
        return false;
    }

    @Override
    public boolean chunkloadWhileOnlineOnly() {
        return configManager.getConfig().isChunkloadWhileOnlineOnly();
    }

    @Override
    public boolean loggingSkipsNonPlayer() {
        return configManager.getConfig().isLoggingSkipsNonPlayer();
    }

    @Override
    public Stream<String> loggingSkipFlagNames() {
        return configManager.getConfig().getLoggingSkipFlags().stream();
    }

    @Override
    public Map<WandType, String> wandItems() {
        return Map.of();
    }

    @Override
    public void onInitialize() {
        loadUnion();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> this.server = server);
        ServerLifecycleEvents.SERVER_STARTED.register(server -> reloadQueryManagers());

        cmdr.register(WorldModCommands.class);

        super.onInitialize();
    }

    public void reloadQueryManagers() {
        this.queryManagers = lib.worldNames()
                .map(world -> new QueryManager(this, world))
                .collect(Streams.append(new QueryManager(this, Region.GLOBAL_REGION_NAME)))
                .collect(Collectors.toMap(mgr -> mgr.getWorldCondition().getWorlds()[0], Function.identity()));
        LOGGER.atLevel(org.slf4j.event.Level.INFO).log("Loaded %d query managers".formatted(queryManagers.size()));
    }
}
