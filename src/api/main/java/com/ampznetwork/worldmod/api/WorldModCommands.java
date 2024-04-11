package com.ampznetwork.worldmod.api;

import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.impl.BasicArea;
import lombok.Value;
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
        return selections.computeIfAbsent(playerId, $->new BasicArea.Builder());
    }

    @Command(permission = WorldMod.Permission.Selection,ephemeral = true)
    public String selection(UUID playerId, @Command.Arg(required = false) @Nullable Shape type) {
        if (type==null) {
            selections.remove(playerId);
            return "";}
        sel(playerId).setShape(type);
        return "Now selecting as "+type.name();
    }

    @Command(permission = WorldMod.Permission.Selection,ephemeral = true)
    public String position(UUID playerId, @Command.Arg int index) {
        var pos = worldMod.getPlayerAdapter().getPosition(playerId);
        sel(playerId).getSpatialAnchors().set(index,pos);
        return "Set position " + index;
    }
}
