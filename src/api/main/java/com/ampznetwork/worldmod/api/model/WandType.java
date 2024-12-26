package com.ampznetwork.worldmod.api.model;

import com.ampznetwork.worldmod.generated.PluginYml.Permission;
import com.ampznetwork.worldmod.generated.PluginYml.Permission.worldmod;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.comroid.api.attr.Named;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public enum WandType implements Named {
    selection("minecraft:stick", worldmod.selection.wand, worldmod.wand.selection),
    lookup("minecraft:blaze_rod", worldmod.lookup.wand, worldmod.wand.lookup);

    String     defaultItem;
    String     configPath = "wand.%s".formatted(name());
    Permission usePermission;
    Permission togglePermission;
}
