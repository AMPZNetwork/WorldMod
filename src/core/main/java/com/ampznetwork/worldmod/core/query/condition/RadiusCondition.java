package com.ampznetwork.worldmod.core.query.condition;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.query.QueryInputData;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import org.comroid.api.func.util.Command;
import org.comroid.api.func.util.Streams;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public record RadiusCondition(int radius) implements QueryCondition {
    @Override
    public boolean test(WorldMod mod, WorldQuery query, QueryInputData data, @Nullable UUID executor) {
        var center = query.getConditions()
                .stream()
                .flatMap(Streams.cast(PositionCondition.class))
                .map(PositionCondition::getA)
                .findAny()
                .or(() -> Optional.ofNullable(executor).map(mod.getLib().getPlayerAdapter()::getPosition))
                .orElseThrow(() -> new Command.Error("Could not find any position to calculate radius around"));
        var position = data.getPosition();

        // check if pos is inside radius around center
        // calculate distance among all coords that are not 0

        // Extract coordinates
        double centerX = center.getX();
        double centerY = center.getY();
        double centerZ = center.getZ();

        double posX = position.getX();
        double posY = position.getY();
        double posZ = position.getZ();

        // Calculate squared distance to avoid computing a square root unless needed
        double distanceSquared = 0.0;

        if (centerX != 0 && posX != 0) {
            distanceSquared += Math.pow(posX - centerX, 2);
        }
        if (centerY != 0 && posY != 0) {
            distanceSquared += Math.pow(posY - centerY, 2);
        }
        if (centerZ != 0 && posZ != 0) {
            distanceSquared += Math.pow(posZ - centerZ, 2);
        }

        // Compare squared distance with squared radius
        return distanceSquared <= Math.pow(radius, 2);
    }
}
