package com.ampznetwork.worldmod.api;

import com.ampznetwork.worldmod.api.database.EntityService;
import com.ampznetwork.worldmod.api.model.adp.PlayerAdapter;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;

import java.util.Collection;
import java.util.Collections;

public interface WorldMod {
    String AddonId = "worldmod";
    String AddonName = "WorldMod";

    @Deprecated(forRemoval = true)
    default Collection<Region> getRegions() {
        return Collections.emptyList();
    }

    @Deprecated(forRemoval = true)
    default Collection<? extends Group> getGroups() {
        return Collections.emptyList();
    }

    EntityService getEntityService();
    PlayerAdapter getPlayerAdapter();

    default boolean addRegion(Region region) {
        //getRegions().add(region);
        return getEntityService().save(region);
    }

    interface Permission {
        String Selection = "worldmod.area.selection";
        String Claiming = "worldmod.region.claim";
    }
}
