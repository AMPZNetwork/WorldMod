package com.ampznetwork.worldmod.core.query.eval.decl.val;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Value
@NonFinal
public class NumberExpression<T extends Number> implements ValueExpression {
    @NotNull T value;

    @Override
    public @NotNull T eval(Map<String, @NotNull Long> vars) {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
