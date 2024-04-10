package org.comroid.mcsd.core.repo.server;

import org.comroid.mcsd.core.entity.server.ServerUptimeEntry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface ServerUptimeRepo extends CrudRepository<ServerUptimeEntry, UUID> {
    @Query("SELECT e FROM ServerUptimeEntry e" +
            " WHERE e.server.id = :id AND e.timestamp < :time" +
            " ORDER BY e.timestamp")
    Iterable<ServerUptimeEntry> since(@Param("id") UUID id, @Param("time") Instant time);
}
