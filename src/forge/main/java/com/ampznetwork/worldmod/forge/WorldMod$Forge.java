package com.ampznetwork.worldmod.forge;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.adp.PlayerAdapter;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;

@Mod(WorldMod.AddonName)
public class WorldMod$Forge implements WorldMod {
    @Override
    public Collection<Region> getRegions() {
        return null;
    }

    @Override
    public Collection<? extends Group> getGroups() {
        return null;
    }

    @Override
    public PlayerAdapter getPlayerAdapter() {
        return null;
    }
}
