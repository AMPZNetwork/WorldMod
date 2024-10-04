package com.ampznetwork.worldmod.api.model.mini;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.comroid.api.attr.Named;
import org.comroid.api.func.util.Bitmask;
import org.comroid.api.text.minecraft.Tellraw;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public enum PlayerRelation implements Named, Bitmask.Attribute<PlayerRelation> {
    ENTITY(Tellraw.Selector.Base.ALL_ENTITIES),
    GUEST(Tellraw.Selector.Base.ALL_PLAYERS),
    MEMBER(Tellraw.Selector.Base.NEAREST_PLAYER),
    ADMIN(Tellraw.Selector.Base.EXECUTOR);
    Tellraw.Selector.Base selector;

    public static PlayerRelation find(Tellraw.Selector.Base base, @Nullable String type) {
        return switch (base) {
            case ALL_ENTITIES -> ENTITY;
            case ALL_PLAYERS, NEAREST_PLAYER -> type == null ? GUEST : valueOf(type.toUpperCase());
            default -> throw new IllegalStateException("Unexpected value: " + base);
        };
    }
}
