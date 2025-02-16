package com.ampznetwork.worldmod.core.query.eval.decl;

import com.ampznetwork.worldmod.core.query.eval.decl.val.VariableExpression;
import com.ampznetwork.worldmod.core.query.eval.model.VarSupplier;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Stream;

@Value
public class OperatorExpression implements Expression {
    @NotNull Expression left, right;
    @NotNull Operator op;

    @Override
    public Stream<VariableExpression> vars() {
        return VarSupplier.concat(left, right);
    }

    @Override
    public @NotNull Object eval(Map<String, @NotNull Long> context) {
        var l = left.eval(context);
        var r = right.eval(context);
        if (l instanceof Number ln && r instanceof Number rn)
            return op.apply(ln, rn);
        return String.valueOf(l) + r;
    }

    @Override
    public String toString() {
        return left.toString() + op.symbol + right;
    }
}
