package com.ampznetwork.worldmod.api;

import com.ampznetwork.worldmod.api.model.Group;
import com.ampznetwork.worldmod.api.model.PlayerAdapter;
import com.ampznetwork.worldmod.api.model.Region;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
