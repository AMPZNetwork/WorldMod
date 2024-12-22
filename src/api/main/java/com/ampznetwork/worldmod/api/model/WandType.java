package com.ampznetwork.worldmod.api.model;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.comroid.api.attr.Named;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public enum WandType implements Named {
    selection("minecraft:stick"),
    lookup("minecraft:blaze_rod");

    String defaultItem;
    String configPath       = "wand.%s".formatted(name());
    String usePermission    = "worldmod.%s.wand".formatted(name());
    String togglePermission = "worldmod.wand.%s".formatted(name());
}
