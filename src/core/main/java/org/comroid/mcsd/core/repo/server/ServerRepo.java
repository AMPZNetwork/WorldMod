package org.comroid.mcsd.core.repo.server;

import jakarta.transaction.Transactional;
import org.comroid.mcsd.core.entity.AbstractEntity;
import org.comroid.mcsd.core.entity.server.Server;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ServerRepo extends AbstractEntity.Repo<Server> {
    @Query("SELECT s FROM Server s WHERE s.agent.id = :agentId")
    Iterable<Server> findAllForAgent(@Param("agentId") UUID agentId);

    @Query("SELECT s FROM Server s WHERE s.agent.id = :agentId AND s.name = :name")
    Optional<Server> findByAgentAndName(@Param("agentId") UUID agentId, @Param("name") String name);

    @Query("SELECT s FROM Server s" +
            " JOIN DiscordModulePrototype dc ON dc.server.id = s.id" +
            " WHERE dc.publicChannelId = :id" +
            " OR dc.moderationChannelId = :id" +
            " OR dc.consoleChannelId = :id")
    Optional<Server> findByDiscordChannel(@Param("id") long id);

    @Modifying
    @Transactional
    @Query("UPDATE Server s SET s.enabled = :enabled WHERE s.id = :srvId")
    void setEnabled(@Param("srvId") UUID srvId, @Param("enabled") boolean enabled);

    @Modifying
    @Transactional
    @Query("UPDATE Server s SET s.maintenance = :maintenance WHERE s.id = :srvId")
    void setMaintenance(@Param("srvId") UUID srvId, @Param("maintenance") boolean maintenance);

    @Modifying
    @Transactional
    @Query("UPDATE BackupModulePrototype bak SET bak.lastBackup = :time WHERE bak.server.id = :srvId")
    void bumpLastBackup(@Param("srvId") UUID srvId, @Param("time") Instant time);

    @Modifying
    @Transactional
    @Query("UPDATE UpdateModulePrototype up SET up.lastUpdate = :time WHERE up.server.id = :srvId")
    void bumpLastUpdate(@Param("srvId") UUID srvId, @Param("time") Instant time);

    default void bumpLastBackup(Server srv) {
        bumpLastBackup(srv.getId(), Instant.now());
    }

    default void bumpLastUpdate(Server srv) {
        bumpLastUpdate(srv.getId(), Instant.now());
    }
}
