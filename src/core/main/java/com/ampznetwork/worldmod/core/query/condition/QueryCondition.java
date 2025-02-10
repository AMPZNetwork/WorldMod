package com.ampznetwork.worldmod.core.query.condition;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.query.QueryInputData;
import com.ampznetwork.worldmod.core.query.ValueComparator;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface QueryCondition {
    boolean SKIP = true;

    /**
     * @param mod      WorldMod instance
     * @param query    The whole query being executed
     * @param data     query input data that should be filtered
     * @param executor if evaluating a lookup, the player that executed the /lookup command, otherwise null
     *
     * @return whether the data passes the condition
     */
    boolean test(WorldMod mod, WorldQuery query, QueryInputData data, @Nullable UUID executor);

    @Nullable
    default ValueComparator comparator() {
        return null;
    }
}
