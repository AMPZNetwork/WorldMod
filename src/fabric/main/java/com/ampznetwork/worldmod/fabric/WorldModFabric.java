package com.ampznetwork.worldmod.fabric;

import com.ampznetwork.libmod.fabric.SubMod$Fabric;
import com.ampznetwork.libmod.fabric.config.Config;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.log.LogEntry;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.core.WorldModCommands;
import com.ampznetwork.worldmod.fabric.adp.internal.FabricEventDispatch;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.comroid.api.java.StackTraceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Getter
public class WorldModFabric extends SubMod$Fabric implements ModInitializer, WorldMod {
    public static final Logger LOGGER = LoggerFactory.getLogger(WorldMod.AddonName);

    static {
        StackTraceUtils.EXTRA_FILTER_NAMES.add("com.ampznetwork");
    }

    private final FabricEventDispatch eventDispatch = new FabricEventDispatch(this);
    private final Collection<Region> regions = new HashSet<>();
    private final Collection<Group>  groups  = new HashSet<>();
    private final WorldModConfig     config  = Config.createAndLoad(WorldModConfig.class);
    private       MinecraftServer    server;

    public WorldModFabric() {
        super(Set.of(Capability.Database), Set.of(Region.class, Group.class, LogEntry.class));
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> this.server = server);

        cmdr.register(WorldModCommands.class);

        super.onInitialize();
    }

    @Override
    public boolean loggingSkipsNonPlayer() {
        return config.isLoggingSkipsNonPlayer();
    }

    @Override
    public Stream<String> loggingSkipFlagNames() {
        return config.getLoggingSkipFlags().stream();
    }
}
