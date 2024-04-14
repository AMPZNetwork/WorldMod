package com.ampznetwork.worldmod.api.model.mini;

import org.comroid.api.attr.Named;
import org.comroid.api.func.util.Bitmask;
import org.comroid.api.text.minecraft.Tellraw;
import org.jetbrains.annotations.Nullable;

public enum PlayerRelation implements Named, Bitmask.Attribute<PlayerRelation> {
    ENTITY,
    GUEST,
    MEMBER,
    OWNER;

    public static PlayerRelation find(Tellraw.Selector.Base base, @Nullable String type) {
        return switch (base) {
            case ALL_ENTITIES -> ENTITY;
            case ALL_PLAYERS, NEAREST_PLAYER -> type == null ? GUEST : valueOf(type.toUpperCase());
            default -> throw new IllegalStateException("Unexpected value: " + base);
        };
    }
}
