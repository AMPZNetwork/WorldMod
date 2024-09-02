package com.ampznetwork.worldmod.core;

import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.util.NameGenerator;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.mini.PlayerRelation;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.api.model.sel.Area;
import com.ampznetwork.worldmod.core.ui.ClaimMenuBook;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.util.TriState;
import org.comroid.annotations.Alias;
import org.comroid.annotations.Default;
import org.comroid.api.attr.Named;
import org.comroid.api.func.util.Command;
import org.comroid.api.text.StringMode;
import org.comroid.api.text.minecraft.McFormatCode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.ampznetwork.worldmod.api.WorldMod.*;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.*;

@UtilityClass
public class WorldModCommands {
    private final Map<UUID, Area.Builder> selections = new ConcurrentHashMap<>();

    @Alias("sel")
    @Command(permission = WorldMod.Permission.Selection, ephemeral = true)
    public String select(UUID playerId, @Command.Arg @Nullable Shape type) {
        if (type == null) {
            selections.remove(playerId);
            return "Selection cleared";
        }
        sel(playerId).setShape(type);
        clearSel(playerId);
        return "Now selecting as " + type.name();
    }

    private Area.Builder sel(UUID playerId) {
        return selections.computeIfAbsent(playerId, $ -> new Area.Builder());
    }

    private void clearSel(UUID playerId) {
        sel(playerId).setSpatialAnchors(new ArrayList<>() {{
            for (int i = 0; i < 8; i++) add(null);
        }});
    }

    @Alias("pos")
    @Command(permission = WorldMod.Permission.Selection, ephemeral = true)
    public static class position {
        @Command
        public static String $(WorldMod worldMod, UUID playerId, @Command.Arg(autoFill = { "1", "2" }) int index) {
            var pos = worldMod.getLib().getPlayerAdapter().getPosition(playerId);
            sel(playerId).getSpatialAnchors().set(index - 1, pos.to4(0));
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
            if (sel.getShape().getAnchorPointCount() != sel.getSpatialAnchors().length)
                throw new Command.Error("Invalid selection; wrong position count");
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
        public static void menu(WorldMod worldMod, UUID playerId, @Nullable Region region) {
            isClaimed(region);
            var menu = new ClaimMenuBook(worldMod.getLib(), region, playerId);
            worldMod.getLib().getPlayerAdapter().openBook(playerId, menu);
        }

        @Command
        public static String name(WorldMod worldMod, UUID playerId, @Nullable Region region, @Nullable @Command.Arg(stringMode = StringMode.GREEDY) String arg) {
            isClaimed(region);
            if (region.getEffectiveFlagValueForPlayer(Flag.Manage, playerId).getState() != TriState.TRUE)
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
        public static String owner(WorldMod worldMod, UUID playerId, @Nullable Region region, @Nullable @Command.Arg String arg) {
            isClaimed(region);
            if (region.getEffectiveFlagValueForPlayer(Flag.Manage, playerId).getState() != TriState.TRUE)
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
                    UUID playerId,
                    @Nullable Region region,
                    @Command.Arg("0") String player,
                    @Command.Arg("1") @Nullable @Default("PlayerRelation.MEMBER") PlayerRelation type
            ) {
                if (type == null) type = PlayerRelation.MEMBER;
                isClaimed(region);
                if (region.getEffectiveFlagValueForPlayer(Flag.Manage, playerId).getState() != TriState.TRUE)
                    notPermitted();
                var targetId = worldMod.getLib().getPlayerAdapter().getId(player);
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
                    UUID playerId,
                    @Nullable Region region,
                    @Command.Arg String player
            ) {
                isClaimed(region);
                if (region.getEffectiveFlagValueForPlayer(Flag.Manage, playerId).getState() != TriState.TRUE)
                    notPermitted();
                var targetId = worldMod.getLib().getPlayerAdapter().getId(player);
                var wasOwner  = region.getOwners().remove(targetId);
                var wasMember = region.getMembers().remove(targetId);
                return "%s was removed from the list of %s".formatted(player, concat(
                        wasOwner ? of(PlayerRelation.ADMIN) : empty(),
                        wasMember ? of(PlayerRelation.MEMBER) : empty())
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
