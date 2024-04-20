package com.ampznetwork.worldmod.api.game;

import com.ampznetwork.worldmod.api.model.mini.OwnedByParty;
import com.ampznetwork.worldmod.api.model.mini.Prioritized;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kyori.adventure.util.TriState;
import org.comroid.api.attr.Described;
import org.comroid.api.attr.Named;
import org.comroid.api.data.seri.type.ValueType;
import org.comroid.api.func.util.Bitmask;
import org.comroid.api.info.Constraint;
import org.comroid.api.text.minecraft.Tellraw;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.comroid.api.data.seri.type.StandardValueType.BOOLEAN;

@Value
@EqualsAndHashCode(of = "name")
public class Flag implements Named, Described, Prioritized {
    private static final Map<String, Flag> $ = new ConcurrentHashMap<>();
    public static final Map<String,Flag> VALUES = Collections.unmodifiableMap($);
    public static final Flag Passthrough = new Flag("passthrough", Long.MAX_VALUE, BOOLEAN, "Passthrough", "Enable to force WorldMod to not handle any events", false);
    public static final Flag Build = new Flag("build", 50, BOOLEAN, "Building", "Enable to force WorldMod to not handle any events", false);
    public static final Flag Interact = new Flag("interact", 30, BOOLEAN, "Interactions", "Enable to allow users to interact with your claim", false);
    public static final Flag Use = new Flag("use", 40, BOOLEAN, "Usage", "Enable to allow users to use items in your claim", false);
    public static final Flag Craft = new Flag("craft", 20, BOOLEAN, "Craft Item", "Enable to allow users to craft specific items in your claim", false);
    public static final Flag Explode = new Flag("explode", 20, BOOLEAN, "Explosions", "Allow or Deny any kinds of Explosions", false);
    public static final Flag Explode_Creeper = new Flag(Explode, "creeper", 20, BOOLEAN, "Creeper Explosions", "Allow or Deny Creeper Explosions", false);
    public static final Flag Enter = new Flag("enter", 70, BOOLEAN, "Enter Area", "Disable to disallow users from entering the area", false);
    public static final Flag Leave = new Flag("leave", 70, BOOLEAN, "Leave Area", "Disable to disallow users from leaving the area", false);

    @Nullable Flag parent;
    @NotNull String name;
    long priority;
    @NotNull ValueType<?> type;
    @Nullable String displayName;
    @Nullable String description;
    @Nullable Object defaultValue;

    public Flag(String name,
                long priority,
                ValueType<?> type,
                @Nullable String displayName,
                @Nullable String description,
                @NotNull Object defaultValue) {
        this(null, name, priority, type, displayName, description, defaultValue);
    }

    public Flag(@Nullable Flag parent,
                @NotNull String name,
                long priority,
                @NotNull ValueType<?> type,
                @Nullable String displayName,
                @Nullable String description,
                @Nullable Object defaultValue) {
        this.parent = parent;
        this.name = name;
        this.priority = priority;
        this.type = type;
        this.displayName = displayName;
        this.description = description;
        this.defaultValue = defaultValue;

        Constraint.decide($.containsKey(getCanonicalName()), "cached Flag '" + getBestName() + "'")
                .setExpected("nonexistent")
                .setActual("exists already")
                .invert().run();
        $.put(getCanonicalName(), this);
    }

    @Override
    public String getAlternateName() {
        return displayName;
    }

    public String getCanonicalName() {
        return parent == null ? name : parent.getCanonicalName() + '.' + name;
    }

    @Data
    @Builder
    @EqualsAndHashCode
    public static class Value implements Prioritized {
        @NotNull Flag flag;
        @NotNull TriState state;
        @Default
        @Nullable String value = null;
        @Default @Deprecated
        long target = Bitmask.combine(Target.Guests, Target.Members);
        @Default
        Set<Tellraw.Selector> selectors = Set.of(Tellraw.Selector.builder()
                .base(Tellraw.Selector.Base.NEAREST_PLAYER)
                .type("guest")
                .build());
        @Default
        boolean force = false;
        @Default
        long priority = 0;

        public long getPriority() {
            return force ? Long.MAX_VALUE : priority;
        }

        @Deprecated
        public boolean appliesToUser(OwnedByParty target, UUID playerId) {
            var owner = target.getOwnerIDs().contains(playerId);
            var member = target.getMemberIDs().contains(playerId);
            var mask = this.target;
            return owner ? Target.Owners.isFlagSet(mask)
                    : member ? Target.Members.isFlagSet(mask)
                    : Target.Guests.isFlagSet(mask);
        }

        @Deprecated
        public enum Target implements Named, Bitmask.Attribute<Target> {
            Guests,
            Members,
            Owners
        }
    }
}
