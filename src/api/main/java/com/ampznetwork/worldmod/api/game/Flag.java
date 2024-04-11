package com.ampznetwork.worldmod.api.game;

import com.ampznetwork.worldmod.api.model.mini.Prioritized;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kyori.adventure.util.TriState;
import org.comroid.api.attr.Described;
import org.comroid.api.attr.Named;
import org.comroid.api.data.seri.type.ValueType;
import org.comroid.api.info.Constraint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.comroid.api.data.seri.type.StandardValueType.BOOLEAN;

@Value
@EqualsAndHashCode(of = "name")
public class Flag implements Named, Described, Prioritized {
    private static final Map<String, Flag> $ = new ConcurrentHashMap<>();
    public static final Map<String,Flag> VALUES = Collections.unmodifiableMap($);
    public static final Flag Passthrough = new Flag("passthrough", 90, BOOLEAN, "Passthrough", "Enable to force WorldMod to not handle any events", false);
    public static final Flag Build = new Flag("build", 50, BOOLEAN, "Building", "Enable to force WorldMod to not handle any events", false);

    @NotNull String name;
    long priority;
    @NotNull ValueType<?> type;
    @Nullable String displayName;
    @Nullable String description;
    @NotNull Object defaultValue;

    public Flag(String name, long priority, ValueType<?> type, @Nullable String displayName, @Nullable String description, @NotNull Object defaultValue) {
        this.name = name;
        this.priority = priority;
        this.type = type;
        this.displayName = displayName;
        this.description = description;
        this.defaultValue = defaultValue;

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
    public class Value implements Prioritized {
        @NotNull TriState state;
        @Nullable String value;
        long priority;

        public Flag flag() {
            return Flag.this;
        }
    }
}
