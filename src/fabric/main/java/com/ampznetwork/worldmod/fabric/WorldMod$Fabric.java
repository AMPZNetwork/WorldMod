package com.ampznetwork.worldmod.fabric;

import com.ampznetwork.libmod.fabric.SubMod$Fabric;
import com.ampznetwork.libmod.fabric.config.Config;
import com.ampznetwork.worldmod.api.WorldMod;
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

@Getter
public class WorldMod$Fabric extends SubMod$Fabric implements ModInitializer, WorldMod {
    public static final Logger LOGGER = LoggerFactory.getLogger(WorldMod.AddonName);

    static {
        StackTraceUtils.EXTRA_FILTER_NAMES.add("com.ampznetwork");
    }

    private final FabricEventDispatch eventDispatch = new FabricEventDispatch(this);
    private final Collection<Region> regions = new HashSet<>();
    private final Collection<Group>  groups  = new HashSet<>();
    private final WorldModConfig     config  = Config.createAndLoad(WorldModConfig.class);
    private       MinecraftServer    server;

    public WorldMod$Fabric() {
        super(Set.of(Capability.Database), Set.of(Region.class, Group.class));
    }

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> this.server = server);

        cmdr.register(WorldModCommands.class);

        super.onInitialize();
    }
}
