package com.ampznetwork.worldmod.core.query.eval.decl.val;

import com.ampznetwork.worldmod.core.query.eval.model.QueryEvalContext;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.comroid.api.info.Range;
import org.jetbrains.annotations.NotNull;

@Value
@NonFinal
public class RangeExpression implements NumericExpression {
    NumberExpression<?> low, high;

    @Override
    public @NotNull Range<Integer> eval(QueryEvalContext context) {
        int l = low.eval(context).intValue(), r = high.eval(context).intValue();
        return new Range<>(l, r);
    }

    @Override
    public String toString() {
        return low + ".." + high;
    }
}
