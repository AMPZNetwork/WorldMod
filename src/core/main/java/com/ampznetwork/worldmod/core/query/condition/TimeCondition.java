package com.ampznetwork.worldmod.core.query.condition;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.core.query.InputData;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public record TimeCondition(Instant since) implements QueryCondition {
    @Override
    public boolean test(WorldMod mod, WorldQuery query, InputData data, @Nullable UUID executor) {
        return since.isAfter(data.getTimestamp());
    }
}
