package com.ampznetwork.worldmod.api;

import com.ampznetwork.libmod.api.LibMod;
import com.ampznetwork.libmod.api.SubMod;
import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.worldmod.api.flag.Flag;
import com.ampznetwork.worldmod.api.model.TextResourceProvider;
import com.ampznetwork.worldmod.api.model.WandType;
import com.ampznetwork.worldmod.api.model.config.WorldModConfigAdapter;
import com.ampznetwork.worldmod.api.model.log.LogEntry;
import com.ampznetwork.worldmod.api.model.query.IQueryManager;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.api.model.sel.Area;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.comroid.api.Polyfill;
import org.comroid.api.data.Vector;
import org.comroid.api.func.util.Command;
import org.comroid.api.func.util.Streams;
import org.comroid.api.info.Log;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Tuple;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface WorldMod extends SubMod, Command.ContextProvider, WorldModConfigAdapter {
    String AddonId   = "worldmod";
    String AddonName = "WorldMod";

    @Contract("->fail")
    static void notPermitted() {
        throw new Command.Error("You are not permitted to perform this action");
    }

    @Contract("null->fail")
    static void isClaimed(Region region) {
        if (region == null) throw new Command.Error("This area is not claimed");
    }

    @Override
    default Class<?> getModuleType() {
        return WorldMod.class;
    }

    @Override
    default Set<Class<? extends DbObject>> getEntityTypes() {
        return Stream.concat(getLib().getEntityTypes().stream(),
                        Stream.of(Region.class, Group.class, LogEntry.class, Area.class))
                .collect(Collectors.toUnmodifiableSet());
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

    Map<String, IQueryManager> getQueryManagers();

    default Optional<WandType> findWandType(String itemResourceKey) {
        return wandItems().entrySet()
                .stream()
                .filter(e -> LibMod.equalResourceKey(itemResourceKey, e.getValue()))
                .findAny()
                .map(Map.Entry::getKey);
    }

    default boolean addRegion(Region region) {
        if (region.findOverlaps(this).findAny().isPresent())
            throw new Command.Error("The selected area is overlapping with another claim");
        try {
            region.getAreas()
                    .stream()
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
        return Stream.concat(getEntityService().getAccessor(Region.TYPE).querySelect("""
                        with
                            inside as (select a.*, ra.* from worldmod_region_areas ra
                                                                 join worldmod_areas a where ra.areas_id = a.id),
                            isMatch as (select Region_id as matchId, false
                                -- Cuboid Matching
                                    or (shape = 0
                                    and (:posX between LEAST(x1,x2) and GREATEST(x1,x2) -- x inside
                                    and (:posY between LEAST(y1,y2) and GREATEST(y1,y2) -- y inside
                                    and (:posZ between LEAST(z1,z2) and GREATEST(z1,z2) -- z inside
                            ))))
                            -- todo add more matching methods
                        as bool from inside)
                        select r.* from worldmod_regions r, isMatch where bool and matchId = r.id and r.worldName = :worldName
                        """,
                Map.of("posX",
                        location.getX(),
                        "posY",
                        location.getY(),
                        "posZ",
                        location.getZ(),
                        "worldName",
                        worldName)), Stream.of(Region.global("world"))).sorted(Region.BY_PRIORITY);
    }

    default Stream<Region> findChunkloadedRegions() {
        var entityService = getEntityService();
        return entityService == null ? Stream.empty() : entityService.getAccessor(Region.TYPE)
                .querySelect("""
                        select r.* from worldmod_regions r
                                inner join worldmod_region_flags rf on rf.id = r.id
                                inner join worldmod_region_group_flags rgf on rgf.id = r.group_id
                            where rf.flag = 'manage.chunkload' or rgf.flag = 'manage.chunkload'
                        """)
                .sorted(Region.BY_PRIORITY)
                .filter(rg -> !chunkloadWhileOnlineOnly() || rg.getMembers()
                        .stream()
                        .map(DbObject::getId)
                        .anyMatch(getPlayerAdapter()::isOnline));
    }

    TextResourceProvider text();

    default Stream<String> flagNames() {
        return Flag.VALUES.values().stream().flatMap(WorldMod::ownAndChildFlagNames).sorted();
    }

    default Map<String, Long> flagLog(@Nullable Player player, @Nullable String target) {
        var query = getEntityService().createQuery(mgr -> {
            var q = "select action, COUNT(*) as count from worldmod_world_log e where serverName = :serverName and (result = 0 or result = 2)";
            if (player != null) q += " and player_id = :playerId";
            if (target != null) q += " and nonPlayerTarget = :target";
            return mgr.createNativeQuery(q + " group by action;", Tuple.class);
        }).setParameter("serverName", getLib().getServerName());
        if (player != null) query.setParameter("playerId", player.getId().toString());
        if (target != null) query.setParameter("target", target);
        var map = Polyfill.<Stream<Tuple>>uncheckedCast(query.getResultStream())
                .collect(Collectors.toMap(it -> it.get("action", String.class),
                        it -> it.get("count", BigInteger.class).longValue()));
        Flag.VALUES.values()
                .stream()
                .map(Flag::getCanonicalName)
                .filter(Predicate.not(map::containsKey))
                .forEach(key -> map.put(key, 0L));
        return map;
    }

    private static Stream<String> ownAndChildFlagNames(Flag flag) {
        var name = flag.getName();
        return Stream.concat(Stream.of(name),
                flag.getChildren().stream().flatMap(WorldMod::ownAndChildFlagNames).map(str -> name + '.' + str));
    }
}
