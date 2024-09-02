package com.ampznetwork.worldmod.api;

import com.ampznetwork.libmod.api.SubMod;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import org.comroid.api.data.Vector;
import org.comroid.api.func.util.Command;
import org.comroid.api.func.util.Streams;
import org.comroid.api.info.Log;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Stream;

public interface WorldMod extends SubMod, Command.ContextProvider {
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

    @Override
    default Stream<Object> expandContext(Object... context) {
        var playerAdapter = getLib().getPlayerAdapter();
        var playerId      = Arrays.stream(context).flatMap(Streams.cast(UUID.class)).findAny().orElseThrow();
        var position      = playerAdapter.getPosition(playerId);
        var worldName     = playerAdapter.getWorldName(playerId);
        return Stream.of(findRegions(position, worldName).findFirst().orElse(null));
    }

    default Stream<Region> findRegions(@NotNull Vector.N3 location, @NotNull String worldName) {
        return Stream.concat(getEntityService().getAccessor(Region.TYPE)
                                .querySelect("""
                                        with parse as (select
                                            JSON_UNQUOTE(JSON_EXTRACT(ra.area, '$.shape')) as shape,
                                            CONVERT(JSON_EXTRACT(ra.area, '$.anchors[0].x'), double) as x1,
                                            CONVERT(JSON_EXTRACT(ra.area, '$.anchors[0].y'), double) as y1,
                                            CONVERT(JSON_EXTRACT(ra.area, '$.anchors[0].z'), double) as z1,
                                            CONVERT(JSON_EXTRACT(ra.area, '$.anchors[1].x'), double) as x2,
                                            CONVERT(JSON_EXTRACT(ra.area, '$.anchors[1].y'), double) as y2,
                                            CONVERT(JSON_EXTRACT(ra.area, '$.anchors[1].z'), double) as z2,
                                            ra.id as id from region_areas ra
                                        ), isMatch as (select id as matchId, false
                                        ## Cuboid Matching part
                                            or (shape = 'Cuboid'
                                              and (:posX between LEAST(x1,x2) and GREATEST(x1,x2) ## x inside
                                              and (:posY between LEAST(y1,y2) and GREATEST(y1,y2) ## y inside
                                              and (:posZ between LEAST(z1,z2) and GREATEST(z1,z2) ## z inside
                                            ))))
                                        ## todo add more matching methods
                                        as bool from parse
                                        ) select r.* from regions r, isMatch where bool and matchId = r.id and r.worldName = :worldName
                                        """, Map.of("posX", location.getX(), "posY", location.getY(), "posZ", location.getZ(), "worldName", worldName)),
                        Stream.of(Region.global("world")))
                .sorted(Region.BY_PRIORITY);
    }

    interface Permission {
        String Selection = "worldmod.selection";
        String Claiming = "worldmod.region.claim";
    }
}
