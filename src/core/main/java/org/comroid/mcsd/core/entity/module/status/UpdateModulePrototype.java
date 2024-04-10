package org.comroid.mcsd.core.entity.module.status;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.comroid.mcsd.core.entity.module.ModulePrototype;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateModulePrototype extends ModulePrototype {
    public static final Duration DefaultUpdatePeriod = Duration.ofDays(7);
    public static final Instant DefaultLastUpdate = Instant.ofEpochMilli(0);

    private @Nullable @Basic Duration updatePeriod;
    private @Nullable Instant lastUpdate;
}
