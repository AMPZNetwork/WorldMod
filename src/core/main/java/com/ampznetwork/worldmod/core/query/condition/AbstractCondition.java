package com.ampznetwork.worldmod.core.query.condition;

import com.ampznetwork.worldmod.api.model.query.ConditionType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PROTECTED)
public abstract class AbstractCondition implements QueryCondition {
    ConditionType type;

    protected AbstractCondition(ConditionType type) {
        this.type = type;
    }

    protected abstract String valueToString();

    @Override
    public final String toString() {
        return type.name().toLowerCase() + '=' + valueToString();
    }
}
