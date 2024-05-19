package com.ampznetwork.worldmod.api;

import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.impl.BasicArea;
import lombok.Value;
import org.comroid.annotations.Alias;
import org.comroid.api.func.util.Command;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Value
public class WorldModCommands {
    WorldMod worldMod;
    Map<UUID, BasicArea.Builder> selections = new ConcurrentHashMap<>();

    private BasicArea.Builder sel(UUID playerId) {
        return selections.computeIfAbsent(playerId, $ -> new BasicArea.Builder());
    }

    @Alias("sel")
    @Command(permission = WorldMod.Permission.Selection, ephemeral = true)
    public String select(UUID playerId, @Command.Arg(required = false) @Nullable Shape type) {
        if (type == null) {
            selections.remove(playerId);
            return "Selection cleared";
        }
        sel(playerId).setShape(type);
        return "Now selecting as " + type.name();
    }

    @Alias("pos")
    @Command(permission = WorldMod.Permission.Selection, ephemeral = true)
    public String position(UUID playerId, @Command.Arg int index) {
        var pos = worldMod.getPlayerAdapter().getPosition(playerId);
        sel(playerId).getSpatialAnchors().set(index, pos);
        return "Set position " + index;
    }

    @Command(permission = WorldMod.Permission.Claiming, ephemeral = true)
    public String claim(UUID playerId, @Command.Arg(required = false) @Nullable String name) {
        if (!selections.containsKey(playerId))
            throw new Command.Error("No area selected!");
        var sel = sel(playerId).build();
        if (sel.getShape().getAnchorPointCount() != sel.getSpatialAnchors().length)
            throw new Command.Error("Invalid selection; wrong position count");
        var world = worldMod.getPlayerAdapter().getWorldName(playerId);
        var rg = Region.builder()
                .area(sel)
                .worldName(world)
                .owner(playerId);
        if (!worldMod.addRegion(rg.build()))
            throw new Command.Error("Could not create claim");
        return "Area claimed!";
    }
}
