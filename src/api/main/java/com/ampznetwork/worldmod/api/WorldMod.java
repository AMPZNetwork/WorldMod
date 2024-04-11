package com.ampznetwork.worldmod.api;

import com.ampznetwork.worldmod.api.model.adp.PlayerAdapter;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;

import java.util.Collection;

public interface WorldMod {
    String AddonId = "worldmod";
    String AddonName = "WorldMod";

    Collection<? extends Region> getRegions();
    Collection<? extends Group> getGroups();

    PlayerAdapter getPlayerAdapter();

    interface Permission {
        String Selection = "worldmod.area.selection";
    }
}
