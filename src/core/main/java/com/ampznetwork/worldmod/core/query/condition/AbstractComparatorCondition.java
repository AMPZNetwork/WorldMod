package com.ampznetwork.worldmod.core.query.condition;

import com.ampznetwork.worldmod.api.model.query.ConditionType;
import com.ampznetwork.worldmod.core.query.WorldQuery;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
public abstract class AbstractComparatorCondition extends AbstractCondition {
    WorldQuery.Comparator comparator;

    protected AbstractComparatorCondition(ConditionType type, WorldQuery.Comparator comparator) {
        super(type);
        this.comparator = comparator;
    }
}
