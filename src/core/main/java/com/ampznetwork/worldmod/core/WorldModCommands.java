package com.ampznetwork.worldmod.core;

import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.util.NameGenerator;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.log.LogEntry;
import com.ampznetwork.worldmod.api.model.mini.PlayerRelation;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.api.model.sel.Area;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import com.ampznetwork.worldmod.core.ui.ClaimMenuBook;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.util.TriState;
import org.comroid.annotations.Alias;
import org.comroid.annotations.Default;
import org.comroid.api.attr.Named;
import org.comroid.api.func.util.Command;
import org.comroid.api.text.StringMode;
import org.comroid.api.text.minecraft.McFormatCode;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static com.ampznetwork.worldmod.api.WorldMod.*;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.*;
import static net.kyori.adventure.text.Component.*;

@UtilityClass
public class WorldModCommands {
    private static final Map<UUID, Area.Builder> selections = new ConcurrentHashMap<>();

    @Alias("sel")
    @Command(permission = WorldMod.Permission.Selection, ephemeral = true)
    public String select(UUID playerId, @Command.Arg @Nullable Shape type) {
        if (type == null) {
            selections.remove(playerId);
            return "Selection cleared";
        }
        sel(playerId).shape(type);
        clearSel(playerId);
        return "Now selecting as " + type.name();
    }

    @Command
    public Component lookup(WorldMod mod, Player player, @Command.Arg(stringMode = StringMode.GREEDY) String query) {
        var parse = WorldQuery.parse(query);
        return mod.getEntityService().getAccessor(LogEntry.TYPE)
                .querySelect(parse.toLookupQuery(mod))
                .collect(Collector.of(Component::text, (txt, log) -> txt.append(text("\n")
                        .append(text(log.getAction()))), ComponentBuilder::append, ComponentBuilder::build));
    }

    public static Area.Builder sel(UUID playerId) {
        return selections.computeIfAbsent(playerId, $ -> Area.builder());
    }

    public static void clearSel(UUID playerId) {
        sel(playerId)
                .x1(null).y1(null).z1(null)
                .x2(null).y2(null).z2(null)
                .x3(null).y3(null).z3(null)
                .x4(null).y4(null).z4(null)
                .x5(null).y5(null).z5(null)
                .x6(null).y6(null).z6(null)
                .x7(null).y7(null).z7(null)
                .x8(null).y8(null).z8(null);
    }

    @Alias("pos")
    @Command(permission = WorldMod.Permission.Selection, ephemeral = true)
    public static class position {
        @Command
        public static String $(WorldMod worldMod, UUID playerId, @Command.Arg(autoFill = { "1", "2" }) int index) {
            var pos = worldMod.getLib().getPlayerAdapter().getPosition(playerId);
            if (index == 1)
                sel(playerId).x1((int) pos.getX()).y1((int) pos.getY()).z1((int) pos.getZ());
            else if (index == 2) sel(playerId).x2((int) pos.getX()).y2((int) pos.getY()).z2((int) pos.getZ());
            return "Set position " + index;
        }

        @Command
        public static String clear(UUID playerId) {
            clearSel(playerId);
            return "Selection cleared";
        }
    }

    @Command
    @Alias("region")
    public static class claim {
        @Command(permission = WorldMod.Permission.Claiming, ephemeral = true)
        public static String $(WorldMod worldMod, UUID playerId, @Nullable @Command.Arg String name) {
            var player = worldMod.getLib().getPlayerAdapter()
                    .getPlayer(playerId).orElseThrow();
            if (!selections.containsKey(playerId))
                throw new Command.Error("No area selected!");
            var sel = sel(playerId).build();
            sel.validateShapeMask();
            var world = worldMod.getLib().getPlayerAdapter().getWorldName(playerId);
            var rg = Region.builder()
                    .area(sel)
                    .worldName(world)
                    .claimOwner(player);
            if (name != null)
                rg.name(name);
            if (!worldMod.addRegion(rg.build()))
                throw new Command.Error("Could not create claim");
            return "Area claimed!";
        }

        @Command(permission = WorldMod.Permission.Claiming, ephemeral = true)
        public static String info(@Nullable Region region) {
            isClaimed(region);
            return Optional.ofNullable(region)
                    .map(rg -> rg.getClaimOwner() != null
                               ? "Claimed by " + rg.getClaimOwner().getName()
                               : "This area belongs to " + rg.getOwners().stream()
                                       .map(Player::getName)
                                       .collect(joining(", ")))
                    .orElse("This area is not claimed");
        }

        @Command(permission = WorldMod.Permission.Claiming, ephemeral = true)
        public static void menu(WorldMod worldMod, Player player, @Nullable Region region) {
            isClaimed(region);
            var menu = new ClaimMenuBook(worldMod.getLib(), region, player);
            worldMod.getLib().getPlayerAdapter().openBook(player, menu);
        }

        @Command
        public static String name(
                WorldMod worldMod, Player player, @Nullable Region region,
                @Nullable @Command.Arg(stringMode = StringMode.GREEDY, required = false) String arg
        ) {
            isClaimed(region);
            if (region.getEffectiveFlagValueForPlayer(Flag.Manage, player).getState() != TriState.TRUE)
                notPermitted();
            if (arg == null)
                arg = NameGenerator.POI.get();
            region.setName(arg);
            worldMod.getEntityService().save(region);
            return "Name was changed to " + arg;
        }

        @Command
        public static String group(WorldMod worldMod, UUID playerId, @Nullable Region region, @Nullable @Command.Arg String arg) {
            throw new Command.Error("Not implemented");
        }

        @Command
        public static String owner(WorldMod worldMod, Player player, @Nullable Region region, @Nullable @Command.Arg String arg) {
            isClaimed(region);
            if (region.getEffectiveFlagValueForPlayer(Flag.Manage, player).getState() != TriState.TRUE)
                notPermitted();
            if (arg == null) {
                region.setClaimOwner(null);
                return "Owner removed";
            }
            var targetId = worldMod.getLib().getPlayerAdapter().getId(arg);
            var target = worldMod.getLib().getPlayerAdapter()
                    .getPlayer(targetId).orElseThrow();
            region.setClaimOwner(target);
            worldMod.getEntityService().save(region);
            return target.getName() + " is now owner of " + region.getBestName();
        }

        @Command
        public static class member {
            @Command
            public static String list(WorldMod worldMod, @Nullable Region region) {
                isClaimed(region);
                return """
                        %sOwners%s: %s
                        %sMembers%s: %s
                        """.formatted(
                        McFormatCode.Aqua, McFormatCode.White, region.getOwners().stream()
                                .map(Player::getName)
                                .collect(joining(", ")),
                        McFormatCode.Green, McFormatCode.White, region.getMembers().stream()
                                .map(Player::getName)
                                .collect(joining(", "))
                );
            }

            @Command
            public static String add(
                    WorldMod worldMod,
                    Player player,
                    @Nullable Region region,
                    @Command.Arg("0") String targetName,
                    @Command.Arg("1") @Nullable @Default("PlayerRelation.MEMBER") PlayerRelation type
            ) {
                if (type == null) type = PlayerRelation.MEMBER;
                isClaimed(region);
                if (region.getEffectiveFlagValueForPlayer(Flag.Manage, player).getState() != TriState.TRUE)
                    notPermitted();
                var targetId = worldMod.getLib().getPlayerAdapter().getId(targetName);
                var target = worldMod.getLib().getPlayerAdapter()
                        .getPlayer(targetId).orElseThrow();
                (switch (type) {
                    case MEMBER -> region.getMembers();
                    case ADMIN -> region.getOwners();
                    default -> throw new Command.Error("cannot add member of this type");
                }).add(target);
                return "%s was added to the list of %ss".formatted(player, type.name().toLowerCase());
            }

            @Command
            public static String remove(
                    WorldMod worldMod,
                    Player player,
                    @Nullable Region region,
                    @Command.Arg String targetName
            ) {
                isClaimed(region);
                if (region.getEffectiveFlagValueForPlayer(Flag.Manage, player).getState() != TriState.TRUE)
                    notPermitted();
                var targetId = worldMod.getLib().getPlayerAdapter().getId(targetName);
                var wasOwner  = region.getOwners().remove(targetId);
                var wasMember = region.getMembers().remove(targetId);
                return "%s was removed from the list of %s".formatted(targetName, concat(
                        wasOwner ? of(PlayerRelation.ADMIN) : Stream.empty(),
                        wasMember ? of(PlayerRelation.MEMBER) : Stream.empty())
                        .map(Named::getName)
                        .map(String::toLowerCase)
                        .map(str -> str + 's')
                        .collect(joining(" and ")));
            }
        }

        @Command
        public static class flag {
            @Command
            public static String set(
                    WorldMod worldMod,
                    UUID playerId,
                    @Nullable Region region,
                    @Command.Arg String name,
                    @Command.Arg String value
            ) {
                throw new Command.Error("Not implemented");
            }
        }
    }
}
