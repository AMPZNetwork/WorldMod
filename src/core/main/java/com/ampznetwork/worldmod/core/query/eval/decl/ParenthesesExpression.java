package com.ampznetwork.worldmod.core.query.eval.decl;

import lombok.Value;
import lombok.experimental.Delegate;

@Value
public class ParenthesesExpression implements Expression {
    @Delegate Expression inner;
}
