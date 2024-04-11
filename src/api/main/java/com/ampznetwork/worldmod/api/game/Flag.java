package com.ampznetwork.worldmod.api.game;

import lombok.Data;
import lombok.Value;
import org.comroid.api.attr.Described;
import org.comroid.api.attr.Named;
import org.comroid.api.data.seri.type.StandardValueType;
import org.comroid.api.data.seri.type.ValueType;
import org.comroid.api.info.Constraint;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.comroid.api.data.seri.type.StandardValueType.*;

@Value
public class Flag implements Named, Described {
    private static final Map<String, Flag> $ = new ConcurrentHashMap<>();
    public static final Map<String,Flag> VALUES = Collections.unmodifiableMap($);
    public static final Flag Passthrough = new Flag("passthrough", BOOLEAN, "Passthrough", "Enable to force WorldMod to not handle any events");
    public static final Flag Build = new Flag("build", BOOLEAN, "Building", "Enable to force WorldMod to not handle any events");

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
