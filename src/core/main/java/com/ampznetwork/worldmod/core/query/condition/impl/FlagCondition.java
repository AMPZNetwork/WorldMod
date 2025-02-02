package com.ampznetwork.worldmod.core.query.condition.impl;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.query.ConditionType;
import com.ampznetwork.worldmod.api.model.query.QueryInputData;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import com.ampznetwork.worldmod.core.query.condition.AbstractCondition;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
public class FlagCondition extends AbstractCondition {
    Flag flag;

    public FlagCondition(Flag flag) {
        super(ConditionType.FLAG);
        this.flag = flag;
    }

    @Override
    public boolean test(WorldMod mod, WorldQuery query, QueryInputData data, @Nullable UUID executor) {
        return data.getAction() == null || flag.equals(data.getAction());
    }

    @Override
    protected String valueToString() {
        return flag.getCanonicalName();
    }
}
