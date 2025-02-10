package com.ampznetwork.worldmod.core.query.condition.impl;

import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.query.ConditionType;
import com.ampznetwork.worldmod.api.model.query.QueryInputData;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import com.ampznetwork.worldmod.core.query.condition.AbstractComparatorCondition;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.stream.Stream;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
public class TargetCondition extends AbstractComparatorCondition {
    String target;

    public TargetCondition(WorldQuery.Comparator comparator, String target) {
        super(ConditionType.TARGET, comparator);
        this.target = target;
    }

    @Override
    public boolean test(WorldMod mod, WorldQuery query, QueryInputData data, @Nullable UUID executor) {
        return Stream.concat(Stream.ofNullable(data.getPlayer()).map(Player::getName), Stream.ofNullable(data.getTargetResourceKey()).map(Object::toString))
                .anyMatch(str -> comparator.test(str, target));
    }

    @Override
    protected String valueToString() {
        return target;
    }
}
