package com.ampznetwork.worldmod.fabric;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.core.WorldModCommands;
import com.ampznetwork.worldmod.core.database.file.LocalEntityService;
import com.ampznetwork.worldmod.core.database.hibernate.HibernateEntityService;
import com.ampznetwork.worldmod.fabric.adp.internal.FabricEventDispatch;
import com.ampznetwork.worldmod.fabric.adp.internal.FabricPlayerAdapter;
import com.ampznetwork.worldmod.fabric.cfg.Config;
import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.comroid.api.func.util.Command;
import org.comroid.api.java.StackTraceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;

@Getter
public class WorldMod$Fabric implements ModInitializer, WorldMod {
    public static final Logger LOGGER = LoggerFactory.getLogger(WorldMod.AddonName);
    static {
        StackTraceUtils.EXTRA_FILTER_NAMES.add("com.ampznetwork");
    }

    private final FabricPlayerAdapter playerAdapter = new FabricPlayerAdapter(this);
    private final FabricEventDispatch eventDispatch = new FabricEventDispatch(this);
    private final Collection<Region> regions = new HashSet<>();
    private final Collection<Group> groups = new HashSet<>();
    private final Config config = Config.createAndLoad();
    private MinecraftServer server;
    private Command.Manager cmdr;
    //private Command.Manager.Adapter$Fabric adapter;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> this.server = server);

        this.cmdr = new Command.Manager();
        cmdr.register(WorldModCommands.class);
        cmdr.register(this);
        cmdr.initialize();

        var srv = config.entityService();
        var db = config.database;
        this.entityService = switch (srv) {
            case File -> new LocalEntityService(this);
            case Database -> new HibernateEntityService(this, db.type(), db.url(), db.username(), db.password());
        };
    }
}
