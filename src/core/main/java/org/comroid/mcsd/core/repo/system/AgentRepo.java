package org.comroid.mcsd.core.repo.system;

import jakarta.transaction.Transactional;
import org.comroid.mcsd.core.entity.AbstractEntity;
import org.comroid.mcsd.core.entity.system.Agent;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AgentRepo extends AbstractEntity.Repo<Agent> {
    @Modifying
    @Transactional
    @Query("UPDATE Agent a SET a.baseUrl = :baseUrl WHERE a.id = :id")
    void setBaseUrl(@Param("id") UUID id, @Param("baseUrl") String baseUrl);

    Optional<Agent> getByIdAndToken(@Param("id") UUID id, @Param("token") String token);
}
