package com.ampznetwork.worldmod.api.model.log;

import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.model.EntityType;
import com.ampznetwork.worldmod.api.model.mini.EventState;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.comroid.api.Polyfill;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.time.Instant;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "worldmod_world_log")
public class LogEntry extends DbObject {
    public static final EntityType<LogEntry, LogEntry.Builder<LogEntry, ?>> TYPE
            = Polyfill.uncheckedCast(new EntityType<>(LogEntry::builder, null, LogEntry.class, LogEntry.Builder.class));
    @NotNull String serverName;
    String worldName;
    String action;
    int x, y, z;
    @Default                      Instant    timestamp       = Instant.now();
    @Nullable @Default @ManyToOne Player     player          = null;
    @Nullable @Default            String     nonPlayerSource = null;
    @Nullable @Default @ManyToOne Player     target          = null;
    @Nullable @Default            String     nonPlayerTarget = null;
    @Nullable @Default            EventState result          = null;

    @PrePersist
    @PreUpdate
    private void validateMutualExclusivity() {
        if (player != null && nonPlayerSource != null) throw new IllegalStateException("Only one of player or nonPlayerSource must be non-null.");
        if (target != null && nonPlayerTarget != null) throw new IllegalStateException("Only one of target or nonPlayerTarget must be non-null.");
    }
}
