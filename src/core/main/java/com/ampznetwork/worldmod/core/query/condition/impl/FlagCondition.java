package com.ampznetwork.worldmod.core.query.condition.impl;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.flag.Flag;
import com.ampznetwork.worldmod.api.model.query.ConditionType;
import com.ampznetwork.worldmod.api.model.query.QueryInputData;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import com.ampznetwork.worldmod.core.query.condition.AbstractCondition;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
public class FlagCondition extends AbstractCondition {
    Flag[] flags;

    public FlagCondition(Flag... flags) {
        super(ConditionType.FLAG);
        this.flags = flags;
    }

    @Override
    public boolean test(WorldMod mod, WorldQuery query, QueryInputData data, @Nullable UUID executor) {
        return data.getAction() == null || Arrays.stream(flags).anyMatch(flag -> flag.equals(data.getAction()));
    }

    @Override
    protected String valueToString() {
        return Arrays.stream(flags).map(Flag::getCanonicalName).collect(Collectors.joining(","));
    }
}
