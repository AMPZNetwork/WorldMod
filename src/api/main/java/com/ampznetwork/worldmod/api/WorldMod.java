package com.ampznetwork.worldmod.api;

import com.ampznetwork.libmod.api.SubMod;
import com.ampznetwork.libmod.core.database.hibernate.PersistenceUnitBase;
import com.ampznetwork.worldmod.api.model.adp.PlayerAdapter;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import org.comroid.api.func.util.Command;
import org.comroid.api.info.Log;
import org.jetbrains.annotations.Contract;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.sql.DataSource;
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

    PlayerAdapter getPlayerAdapter();

    default boolean addRegion(Region region) {
        try {
            getEntityService().save(region);
            return true;
        } catch (Throwable t) {
            Log.at(Level.WARNING, "Could not save region " + region, t);
            return false;
        }
    }

    @Override
    default Class<?> getModuleType() {
        return WorldMod.class;
    }

    @Override
    default PersistenceUnitInfo createPersistenceUnit(DataSource dataSource) {
        return new PersistenceUnitBase("WorldMod", WorldMod.class, dataSource, getEntityTypes().toArray(new Class[0]));
    }

    interface Permission {
        String Selection = "worldmod.area.selection";
        String Claiming = "worldmod.region.claim";
    }
}
