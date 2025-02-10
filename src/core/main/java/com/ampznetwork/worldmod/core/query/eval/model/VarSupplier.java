package com.ampznetwork.worldmod.core.query.eval.model;

import com.ampznetwork.worldmod.core.query.eval.decl.val.VariableExpression;

import java.util.stream.Stream;

public interface VarSupplier {
    static Stream<VariableExpression> concat(VarSupplier x, VarSupplier y) {
        return concat(x, y.vars());
    }

    static Stream<VariableExpression> concat(VarSupplier x, Stream<VariableExpression> y) {
        return Stream.concat(x.vars(), y);
    }

    default Stream<VariableExpression> vars() {
        return Stream.empty();
    }

    default Stream<VariableExpression> append(VarSupplier other) {
        return append(other.vars());
    }

    default Stream<VariableExpression> append(Stream<VariableExpression> other) {
        return concat(this, other);
    }
}
