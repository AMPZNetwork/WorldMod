package com.ampznetwork.worldmod.core.query.eval.decl.val;

import com.ampznetwork.worldmod.core.query.eval.model.QueryEvalContext;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;

@Value
@NonFinal
public class NumberExpression<T extends Number> implements NumericExpression {
    @NotNull T value;

    @Override
    public @NotNull T eval(QueryEvalContext context) {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
