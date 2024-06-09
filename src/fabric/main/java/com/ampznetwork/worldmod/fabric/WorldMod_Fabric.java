package com.ampznetwork.worldmod.fabric;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.database.EntityService;
import com.ampznetwork.worldmod.api.model.adp.PlayerAdapter;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import lombok.Value;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@Value
public class WorldMod_Fabric implements ModInitializer, WorldMod {
    public static final Logger LOGGER = LoggerFactory.getLogger(WorldMod.AddonName);
    EntityService entityService;
    PlayerAdapter playerAdapter;

    @Override
    public void onInitialize() {
    }

    @Override
    public Collection<Region> getRegions() {
        return null;
    }

    @Override
    public Collection<? extends Group> getGroups() {
        return null;
    }
}
