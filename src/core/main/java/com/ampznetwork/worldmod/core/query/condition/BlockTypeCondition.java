package com.ampznetwork.worldmod.core.query.condition;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.core.query.InputData;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record BlockTypeCondition(String identifier, WorldQuery.Comparator comparator) implements QueryCondition {
    @Override
    public boolean test(WorldMod mod, WorldQuery query, InputData data, @Nullable UUID executor) {
        var targetResourceKey = data.getTargetResourceKey();
        return targetResourceKey == null || (
                comparator.test(identifier, identifier.contains(":")
                                            ? targetResourceKey.asString()
                                            : targetResourceKey.value()));
    }
}
