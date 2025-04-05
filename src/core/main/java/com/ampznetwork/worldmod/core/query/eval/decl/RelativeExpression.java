package com.ampznetwork.worldmod.core.query.eval.decl;

import com.ampznetwork.worldmod.core.query.eval.decl.val.NumericExpression;
import com.ampznetwork.worldmod.core.query.eval.decl.val.ValueExpression;
import com.ampznetwork.worldmod.core.query.eval.model.QueryEvalContext;
import lombok.Value;
import org.comroid.api.info.Range;
import org.jetbrains.annotations.NotNull;

@Value
public class RelativeExpression implements ValueExpression {
    NumericExpression delta;

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Object eval(QueryEvalContext context) {
        var delta          = this.delta.eval(context);
        var player         = context.getPlayer();
        var relativeTarget = context.getRelativeTarget();
        if (player == null || relativeTarget == null) return delta;
        var base = relativeTarget.get(context.getMod().getPlayerAdapter().getPosition(player.getId()));

        return switch (delta) {
            case Range<?> range -> ((Range<Number>) range).withStart(Operator.PLUS.apply(base, range.getStart()))
                    .withEnd(Operator.PLUS.apply(base, range.getEnd()));
            case Number number -> Operator.PLUS.apply(base, number);
            default -> throw new IllegalStateException("Unexpected value: " + delta);
        };
    }

    @Override
    public String toString() {
        return "~" + delta;
    }
}
