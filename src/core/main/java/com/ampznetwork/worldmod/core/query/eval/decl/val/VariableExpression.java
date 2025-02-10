package com.ampznetwork.worldmod.core.query.eval.decl.val;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Stream;

@Value
public class VariableExpression implements ValueExpression {
    @NotNull String key;

    @Override
    public Stream<VariableExpression> vars() {
        return Stream.of(this);
    }

    @Override
    public @NotNull Object eval(Map<String, @NotNull Long> vars) {
        return vars.get(key);
    }
}
