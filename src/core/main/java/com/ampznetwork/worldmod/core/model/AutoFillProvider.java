package com.ampznetwork.worldmod.core.model;

import com.ampznetwork.libmod.api.SubMod;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.worldmod.api.flag.Flag;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.generated.PluginYml;
import org.comroid.annotations.Instance;
import org.comroid.api.attr.Named;
import org.comroid.api.func.util.Command;
import org.comroid.api.func.util.Streams;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.comroid.api.Polyfill.*;

public interface AutoFillProvider {
    enum RegionNames implements Command.AutoFillProvider {
        @Instance INSTANCE;

        @Override
        public Stream<String> autoFill(Command.Usage usage, String argName, String currentValue) {
            var mod     = usage.getContext().stream().flatMap(Streams.cast(SubMod.class)).findAny().orElseThrow();
            var regions = mod.getEntityService().getAccessor(Region.TYPE);
            return usage.getContext()
                    .stream()
                    .flatMap(Streams.cast(Player.class))
                    .findAny()
                    .filter(Predicate.not(player -> mod.getPlayerAdapter()
                            .checkPermission(player.getId(), PluginYml.Permission.worldmod.ADMIN)
                            .toBooleanOrElse(false)))
                    // stream own region names
                    .map(player -> regions.querySelect("""
                                    select r.* from worldmod_regions r where r.NAME like :regionName and r.claimOwner_id = :ownerId """,
                            Map.of("ownerId", player.getId(), "regionName", currentValue + ".*")).map(Region::getName))
                    .orElseGet(() -> regions.querySelect("""
                                            select r.* from worldmod_regions r where r.NAME like :regionName and r.claimOwner_id is not null""",
                                    Map.of("regionName", currentValue + ".*"))
                            .map(rg -> rg.getClaimOwner().getName() + '-' + rg.getName()))
                    .collect(Collector.of(HashMap<String, @NotNull Long>::new,
                            (map, key) -> map.compute(key, ($, n) -> n == null ? 1 : n + 1),
                            (l, r) -> {
                                l.putAll(r);
                                return l;
                            },
                            map -> map.entrySet()
                                    .stream()
                                    .flatMap(e -> e.getValue() == 1
                                                  ? Stream.of(e.getKey())
                                                  : LongStream.range(0, e.getValue())
                                                          .mapToObj(n -> e.getKey() + '-' + n))));
        }
    }

    enum Groups implements Command.AutoFillProvider {
        @Instance INSTANCE;

        @Override
        public Stream<String> autoFill(Command.Usage usage, String argName, String currentValue) {
            return usage.getContext()
                    .stream()
                    .flatMap(Streams.cast(SubMod.class))
                    .flatMap(mod -> mod.getEntityService().getAccessor(Group.TYPE).all())
                    .flatMap(Streams.cast(Named.class))
                    .map(Named::getName);
        }
    }

    @Deprecated
    enum RegionsAndGroups implements Command.AutoFillProvider {
        @Instance INSTANCE;

        @Override
        public Stream<String> autoFill(Command.Usage usage, String argName, String currentValue) {
            return usage.getContext()
                    .stream()
                    .flatMap(Streams.cast(SubMod.class))
                    .flatMap(mod -> Stream.of(Region.TYPE, Group.TYPE)
                            .flatMap(type -> mod.getEntityService().getAccessor(uncheckedCast(type)).all()))
                    .flatMap(Streams.cast(Named.class))
                    .map(Named::getName);
        }
    }

    enum Flags implements Command.AutoFillProvider {
        @Instance INSTANCE;

        @Override
        public Stream<String> autoFill(Command.Usage usage, String argName, String currentValue) {
            return Flag.VALUES.keySet()
                    .stream()
                    .map(str -> str.contains(".") ? str.substring(0, str.indexOf(".") + 1) : str);
        }
    }
}
