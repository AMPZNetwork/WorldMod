package org.comroid.mcsd.core.entity.module.local;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.comroid.mcsd.core.entity.module.console.ConsoleModulePrototype;
import org.jetbrains.annotations.Nullable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocalExecutionModulePrototype extends ConsoleModulePrototype {
    public static final @Column(columnDefinition = "TEXT") String DefaultCustomCommand = null;
    public static final byte DefaultRamGB = 4;

    private @Nullable @Column(columnDefinition = "TEXT") String customCommand;
    private @Nullable Byte ramGB;
}
