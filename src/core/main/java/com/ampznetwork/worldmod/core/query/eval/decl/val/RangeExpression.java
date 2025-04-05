package com.ampznetwork.worldmod.core.query.eval.decl.val;

import com.ampznetwork.worldmod.core.query.eval.model.QueryEvalContext;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.comroid.api.info.Range;
import org.jetbrains.annotations.NotNull;

@Value
@NonFinal
public class RangeExpression<T extends Number> implements NumericExpression {
    NumberExpression<T> low, high;

    @Override
    public @NotNull Range<T> eval(QueryEvalContext context) {
        T l = low.eval(context), r = high.eval(context);
        return new Range<>(l, r);
    }

    @Override
    public String toString() {
        return low + ".." + high;
    }
}
