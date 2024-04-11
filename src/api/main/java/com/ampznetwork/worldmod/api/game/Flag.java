package com.ampznetwork.worldmod.api.game;

import lombok.Data;
import lombok.Value;
import org.comroid.api.attr.Described;
import org.comroid.api.attr.Named;
import org.comroid.api.data.seri.type.ValueType;
import org.comroid.api.info.Constraint;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Value
public class Flag implements Named, Described {
    private static final Map<String, Flag> $ = new ConcurrentHashMap<>();
    public static final Map<String,Flag> VALUES = Collections.unmodifiableMap($);

    String name;
    ValueType<?> type;
    @Nullable String displayName;
    @Nullable String description;

    public Flag(String name, ValueType<?> type, @Nullable String displayName, @Nullable String description) {
        this.name = name;
        this.type = type;
        this.displayName = displayName;
        this.description = description;

        Constraint.decide($.containsKey(name), "cached Flag '" + getBestName() + "'")
                .setExpected("nonexistent")
                .setActual("exists already")
                .invert().run();
        $.put(name, this);
    }

    @Override
    public String getAlternateName() {
        return displayName;
    }

    @Data
    public class Value {
        Object value;

        public Flag flag() {
            return Flag.this;
        }
    }
}
