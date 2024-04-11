package com.ampznetwork.worldmod.api;

import com.ampznetwork.worldmod.api.model.Group;
import com.ampznetwork.worldmod.api.model.Region;

import java.util.Collection;

public interface WorldMod {
    String AddonId = "worldmod";
    String AddonName = "WorldMod";

    Collection<? extends Region> getRegions();
    Collection<? extends Group> getGroups();
}
