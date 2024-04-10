package org.comroid.mcsd.core.entity.server;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.comroid.mcsd.api.model.Status;

import java.time.Instant;

@Data
@Entity
@NoArgsConstructor
public class ServerUptimeEntry {
    private static final Object lock = new Object();

    // todo
    // this might be an issue with multiple agents
    // although only in rare cases they should be interfering with each other
    // as in: have the VERY SAME timestamp
    private @Id Instant timestamp;
    private @ManyToOne Server server;
    private Status status;
    private int players;
    private long ramKB;

    public ServerUptimeEntry(Server server, Status status, int players, long ramKB) {
        synchronized (lock) {
            timestamp = Instant.now();
        }
        this.server = server;
        this.status = status;
        this.players = players;
        this.ramKB = ramKB;
    }
}
