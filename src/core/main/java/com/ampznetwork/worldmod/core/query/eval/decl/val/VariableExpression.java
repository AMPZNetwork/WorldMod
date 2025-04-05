package com.ampznetwork.worldmod.core.query.eval.decl.val;

import com.ampznetwork.worldmod.core.query.eval.model.QueryEvalContext;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

@Value
public class VariableExpression implements ValueExpression {
    @NotNull String key;

    @Override
    public Stream<VariableExpression> vars() {
        return Stream.of(this);
    }

    @Override
    public @NotNull Object eval(QueryEvalContext context) {
        return context.getFlagLog().get(key);
    }

    @Override
    public String toString() {
        return key;
    }
}
