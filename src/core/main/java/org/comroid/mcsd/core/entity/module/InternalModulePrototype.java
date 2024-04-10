package org.comroid.mcsd.core.entity.module;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.comroid.mcsd.core.model.ModuleType;

/** stub */
@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class InternalModulePrototype extends ModulePrototype {
    @Override
    public ModuleType<?, ?> getDtype() {
        return ModuleType.Internal;
    }
}
