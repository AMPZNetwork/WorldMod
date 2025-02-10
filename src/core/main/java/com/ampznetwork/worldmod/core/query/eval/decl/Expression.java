package com.ampznetwork.worldmod.core.query.eval.decl;

import com.ampznetwork.worldmod.core.query.eval.ExpressionParser;
import com.ampznetwork.worldmod.core.query.eval.model.VarSupplier;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.StringReader;
import java.util.Map;

public interface Expression extends VarSupplier {
    @SneakyThrows
    static Expression parse(String expr) {
        try (StringReader sr = new StringReader(expr)) {
            return new ExpressionParser(sr).expr();
        }
    }

    @NotNull Object eval(Map<String, @NotNull Long> context);
}
