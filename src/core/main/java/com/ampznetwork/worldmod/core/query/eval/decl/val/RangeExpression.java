package com.ampznetwork.worldmod.core.query.eval.decl.val;

import lombok.Value;
import lombok.experimental.NonFinal;
import org.comroid.api.info.Range;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Value
@NonFinal
public class RangeExpression<T extends Number> implements ValueExpression {
    NumberExpression<T> low, high;

    @Override
    public @NotNull Range<T> eval(Map<String, @NotNull Long> context) {
        T l = low.eval(context), r = high.eval(context);
        return new Range<>(l, r);
    }

    @Override
    public String toString() {
        return low + ".." + high;
    }
}
