package com.ampznetwork.worldmod.core.query;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.core.query.condition.QueryCondition;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record FlagCondition(Flag flag) implements QueryCondition {
    @Override
    public boolean test(WorldMod mod, WorldQuery query, InputData data, @Nullable UUID executor) {
        return data.getAction() == null || flag.equals(data.getAction());
    }
}
