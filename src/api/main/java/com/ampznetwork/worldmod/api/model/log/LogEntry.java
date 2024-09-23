package com.ampznetwork.worldmod.api.model.log;

import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.model.EntityType;
import com.ampznetwork.libmod.api.model.convert.VectorConverter;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.mini.EventState;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.comroid.api.Polyfill;
import org.comroid.api.data.Vector;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.Instant;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry extends DbObject {
    public static final EntityType<LogEntry, LogEntry.Builder<LogEntry, ?>> TYPE
            = Polyfill.uncheckedCast(new EntityType<>(LogEntry::builder, null, LogEntry.class, LogEntry.Builder.class));
    String worldName;
    @Convert(converter = VectorConverter.class) Vector.N3  position;
    @Convert(converter = Flag.Converter.class)  Flag       action;
    @Default                                    Instant    timestamp       = Instant.now();
    @Nullable @Default @ManyToOne               Player     player          = null;
    @Nullable @Default                          String     nonPlayerSource = null;
    @Nullable @Default @ManyToOne               Player     target          = null;
    @Nullable @Default                          String     nonPlayerTarget = null;
    @Nullable @Default                          EventState result          = null;

    @PrePersist
    @PreUpdate
    private void validateMutualExclusivity() {
        if (player != null && nonPlayerSource != null) throw new IllegalStateException("Only one of player or nonPlayerSource must be non-null.");
        if (target != null && nonPlayerTarget != null) throw new IllegalStateException("Only one of target or nonPlayerTarget must be non-null.");
    }
}