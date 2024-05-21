package com.ampznetwork.worldmod.api;

import com.ampznetwork.worldmod.api.database.EntityService;
import com.ampznetwork.worldmod.api.model.adp.PlayerAdapter;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;

import java.util.Collection;

public interface WorldMod {
    String AddonId = "worldmod";
    String AddonName = "WorldMod";

    Collection<Region> getRegions();

    Collection<? extends Group> getGroups();

    EntityService getEntityService();
    PlayerAdapter getPlayerAdapter();

    default boolean addRegion(Region region) {
        var regions = getRegions();
        regions.add(region);
        getEntityService().save(region);
        return regions.contains(region);
    }

    interface Permission {
        String Selection = "worldmod.area.selection";
        String Claiming = "worldmod.region.claim";
    }
}
