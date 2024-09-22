package com.ampznetwork.worldmod.core.query.condition;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.core.query.InputData;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface QueryCondition {
    boolean SKIP = true;

    @Nullable
    default WorldQuery.Comparator comparator() {
        return null;
    }

    /**
     * @param mod      WorldMod instance
     * @param query    The whole query being executed
     * @param data     query input data that should be filtered
     * @param executor if evaluating a lookup, the player that executed the /lookup command, otherwise null
     *
     * @return whether the data passes the condition
     */
    boolean test(WorldMod mod, WorldQuery query, InputData data, @Nullable UUID executor);
}
