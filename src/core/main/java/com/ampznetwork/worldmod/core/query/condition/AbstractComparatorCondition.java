package com.ampznetwork.worldmod.core.query.condition;

import com.ampznetwork.worldmod.api.model.query.ConditionType;
import com.ampznetwork.worldmod.core.query.ValueComparator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
public abstract class AbstractComparatorCondition extends AbstractCondition {
    ValueComparator comparator;

    protected AbstractComparatorCondition(ConditionType type, ValueComparator comparator) {
        super(type);
        this.comparator = comparator;
    }
}
