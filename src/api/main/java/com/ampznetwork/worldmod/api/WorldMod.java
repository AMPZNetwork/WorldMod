package com.ampznetwork.worldmod.api;

import com.ampznetwork.libmod.api.LibMod;
import com.ampznetwork.libmod.api.SubMod;
import com.ampznetwork.worldmod.api.model.TextResourceProvider;
import com.ampznetwork.worldmod.api.model.WandType;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.api.model.sel.Area;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.comroid.api.data.Vector;
import org.comroid.api.func.util.Command;
import org.comroid.api.func.util.Streams;
import org.comroid.api.info.Log;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Stream;

public interface WorldMod extends SubMod, Command.ContextProvider {
    String AddonId   = "worldmod";
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

    @Override
    default Class<?> getModuleType() {
        return WorldMod.class;
    }

    @Override
    default TextColor getThemeColor() {
        return NamedTextColor.LIGHT_PURPLE;
    }

    @Override
    default Stream<Object> expandContext(Object... context) {
        var playerAdapter = getLib().getPlayerAdapter();
        var playerId      = Arrays.stream(context).flatMap(Streams.cast(UUID.class)).findAny().orElseThrow();
        var position      = playerAdapter.getPosition(playerId);
        var worldName     = playerAdapter.getWorldName(playerId);
        return Stream.of(findRegions(position, worldName).findFirst().orElse(null));
    }

    boolean loggingSkipsNonPlayer();

    Stream<String> loggingSkipFlagNames();

    Map<WandType, String> wandItems();

    default Optional<WandType> findWandType(String itemResourceKey) {
        return wandItems().entrySet().stream()
                .filter(e -> LibMod.equalResourceKey(itemResourceKey, e.getValue()))
                .findAny()
                .map(Map.Entry::getKey);
    }

    default boolean addRegion(Region region) {
        if (region.findOverlaps(this).findAny().isPresent())
            throw new Command.Error("The selected area is overlapping with another claim");
        try {
            region.getAreas().stream()
                    .filter(a -> getEntityService().getAccessor(Area.TYPE).get(a.getId()).isEmpty())
                    .forEach(getEntityService()::save);
            getEntityService().save(region);
            return true;
        } catch (Throwable t) {
            Log.at(Level.WARNING, "Could not save region " + region, t);
            return false;
        }
    }

    default Stream<Region> findRegions(@NotNull Vector.N3 location, @NotNull String worldName) {
        return Stream.concat(
                        getEntityService().getAccessor(Region.TYPE).querySelect("""
                                with inside as (select a.*, ra.*
                                from worldmod_region_areas ra
                                join worldmod_areas a where ra.areas_id = a.id
                                ), isMatch as (select Region_id as matchId, false
                                ## Cuboid Matching
                                or (shape = 0
                                and (:posX between LEAST(x1,x2) and GREATEST(x1,x2) ## x inside
                                and (:posY between LEAST(y1,y2) and GREATEST(y1,y2) ## y inside
                                and (:posZ between LEAST(z1,z2) and GREATEST(z1,z2) ## z inside
                                ))))
                                ## todo add more matching methods
                                as bool from inside
                                ) select r.* from worldmod_regions r, isMatch where bool and matchId = r.id and r.worldName = :worldName
                                """, Map.of("posX", location.getX(), "posY", location.getY(), "posZ", location.getZ(), "worldName", worldName)),
                        Stream.of(Region.global("world")))
                .sorted(Region.BY_PRIORITY);
    }

    TextResourceProvider text();
}
