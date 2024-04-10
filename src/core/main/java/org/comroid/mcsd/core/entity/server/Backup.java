package org.comroid.mcsd.core.entity.server;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.comroid.mcsd.core.entity.AbstractEntity;

import java.time.Duration;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Backup extends AbstractEntity {
    private Instant timestamp;
    private @ManyToOne Server server;
    private long sizeKb;
    private Duration duration;
    private String file;
    private boolean important;
}
