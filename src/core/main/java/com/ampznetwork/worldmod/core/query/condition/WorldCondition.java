package com.ampznetwork.worldmod.core.query.condition;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.mini.QueryInputData;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record WorldCondition(String worldName, WorldQuery.Comparator comparator) implements QueryCondition {
    @Override
    public boolean test(WorldMod mod, WorldQuery query, QueryInputData data, @Nullable UUID executor) {
        return comparator.test(worldName,data.getWorldName());
    }
}
