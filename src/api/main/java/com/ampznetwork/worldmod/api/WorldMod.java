package com.ampznetwork.worldmod.api;

import com.ampznetwork.worldmod.api.database.EntityService;
import com.ampznetwork.worldmod.api.model.adp.PlayerAdapter;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import org.comroid.api.func.util.Command;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.Collections;

public interface WorldMod {
    String AddonId = "worldmod";
    String AddonName = "WorldMod";

    @Contract("->fail")
    static void notPermitted() {
        throw new Command.Error("You are not permitted to perform this action");
    }

    @Contract("null->fail")
    static void isClaimed(Region region) {
        if (region == null)
            throw new Command.Error("This area is not claimed");
    }

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
