package org.comroid.mcsd.core.entity.module;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class FileModulePrototype extends ModulePrototype {
    public static final String DefaultDirectory = "~/minecraft";
    public static final boolean DefaultForceCustomJar = false;
    public static final String DefaultBackupsDir = "~/backups";

    private @Nullable String directory;
    private @Nullable Boolean forceCustomJar;
    private @Nullable String backupsDir;
}
