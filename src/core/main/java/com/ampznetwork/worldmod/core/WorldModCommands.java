package com.ampznetwork.worldmod.core;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.api.model.sel.Area;
import lombok.Value;
import lombok.experimental.UtilityClass;
import org.comroid.annotations.Alias;
import org.comroid.api.func.util.Command;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@UtilityClass
public class WorldModCommands {
    public WorldMod worldMod;
    private final Map<UUID, Area.Builder> selections = new ConcurrentHashMap<>();

    private Area.Builder sel(UUID playerId) {
        return selections.computeIfAbsent(playerId, $ -> new Area.Builder());
    }

    @Alias("sel")
    @Command(permission = WorldMod.Permission.Selection, ephemeral = true)
    public static String select(UUID playerId, @Command.Arg(required = false) @Nullable Shape type) {
        if (type == null) {
            selections.remove(playerId);
            return "Selection cleared";
        }
        sel(playerId).setShape(type);
        return "Now selecting as " + type.name();
    }

    @Alias("pos")
    @Command(permission = WorldMod.Permission.Selection, ephemeral = true)
    public static String position(UUID playerId, @Command.Arg int index) {
        var pos = worldMod.getPlayerAdapter().getPosition(playerId);
        sel(playerId).getSpatialAnchors().set(index, pos.to4(0));
        return "Set position " + index;
    }

    @Command
    public static class claim {
        @Command(permission = WorldMod.Permission.Claiming, ephemeral = true)
        public static String _(UUID playerId) {
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
        public static String info(UUID playerId) {
            var pos = worldMod.getPlayerAdapter().getPosition(playerId);
            var players = worldMod.getPlayerAdapter();
            return worldMod.getEntityService().findRegion(pos, players.getWorldName(playerId))
                    .map(region -> region.getClaimOwner() != null
                            ? "Claimed by " + players.getName(region.getClaimOwner())
                            : "This area belongs to " + region.getOwnerIDs().stream()
                            .map(players::getName)
                            .collect(Collectors.joining(", ")))
                    .orElse("This area is not claimed");
        }
    }
}
