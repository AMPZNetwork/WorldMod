package com.ampznetwork.worldmod.api;

import com.ampznetwork.libmod.api.SubMod;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import org.comroid.api.func.util.Command;
import org.comroid.api.info.Log;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;

public interface WorldMod extends SubMod {
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

    @Override
    default Class<?> getModuleType() {
        return WorldMod.class;
    }

    default boolean addRegion(Region region) {
        try {
            getEntityService().save(region);
            return true;
        } catch (Throwable t) {
            Log.at(Level.WARNING, "Could not save region " + region, t);
            return false;
        }
    }

    interface Permission {
        String Selection = "worldmod.selection";
        String Claiming = "worldmod.region.claim";
    }
}
