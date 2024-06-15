package com.ampznetwork.worldmod.core;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.api.model.sel.Area;
import com.ampznetwork.worldmod.core.ui.ClaimMenuBook;
import lombok.experimental.UtilityClass;
import org.comroid.annotations.Alias;
import org.comroid.api.func.util.Command;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@UtilityClass
public class WorldModCommands {
    private final Map<UUID, Area.Builder> selections = new ConcurrentHashMap<>();

    private Area.Builder sel(UUID playerId) {
        return selections.computeIfAbsent(playerId, $ -> new Area.Builder());
    }

    private void clearSel(UUID playerId) {
        sel(playerId).setSpatialAnchors(new ArrayList<>() {{
            for (int i = 0; i < 8; i++) add(null);
        }});
    }

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

    @Alias("pos")
    @Command(permission = WorldMod.Permission.Selection, ephemeral = true)
    public static class position {
        @Command
        public static String $(WorldMod worldMod, UUID playerId, @Command.Arg(autoFill = {"1", "2"}) int index) {
            var pos = worldMod.getPlayerAdapter().getPosition(playerId);
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
    @Alias("claim")
    public static class region {
        @Command(permission = WorldMod.Permission.Claiming, ephemeral = true)
        public static String $(WorldMod worldMod, UUID playerId) {
            if (!selections.containsKey(playerId))
                throw new Command.Error("No area selected!");
            var sel = sel(playerId).build();
            if (sel.getShape().getAnchorPointCount() != sel.getSpatialAnchors().length)
                throw new Command.Error("Invalid selection; wrong position count");
            var world = worldMod.getPlayerAdapter().getWorldName(playerId);
            var rg = Region.builder()
                    .area(sel)
                    //.name(UUID.randomUUID().toString())
                    .worldName(world)
                    .claimOwner(playerId);
            if (!worldMod.addRegion(rg.build()))
                throw new Command.Error("Could not create claim");
            return "Area claimed!";
        }

        @Command(permission = WorldMod.Permission.Claiming, ephemeral = true)
        public static String info(WorldMod worldMod, @Nullable Region region) {
            var players = worldMod.getPlayerAdapter();
            return Optional.ofNullable(region)
                    .map(rg -> rg.getClaimOwner() != null
                            ? "Claimed by " + players.getName(rg.getClaimOwner())
                            : "This area belongs to " + rg.getOwnerIDs().stream()
                            .map(players::getName)
                            .collect(Collectors.joining(", ")))
                    .orElse("This area is not claimed");
        }

        @Command(permission = WorldMod.Permission.Claiming, ephemeral = true)
        public static void menu(WorldMod worldMod, UUID playerId, @Nullable Region region) {
            if (region == null)
                throw new Command.Error("This area is not claimed");
            var menu = new ClaimMenuBook(worldMod, region, playerId);
            worldMod.getPlayerAdapter().openBook(playerId, menu);
        }
    }
}
