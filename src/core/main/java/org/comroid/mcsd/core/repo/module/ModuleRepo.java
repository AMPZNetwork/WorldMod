package org.comroid.mcsd.core.repo.module;

import jakarta.transaction.Transactional;
import org.comroid.mcsd.core.entity.AbstractEntity;
import org.comroid.mcsd.core.entity.module.ModulePrototype;
import org.comroid.mcsd.core.model.ModuleType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ModuleRepo<T extends ModulePrototype> extends AbstractEntity.Repo<T> {
    Iterable<T> findAllByServerId(UUID serverId);
    Optional<T> findByServerIdAndDtype(UUID serverId, ModuleType<?,?> dtype);

    @Modifying
    @Transactional
    @Query("UPDATE ModulePrototype p SET p.enabled = :state WHERE p.id = :id")
    void updateModuleState(@Param("id") UUID id, @Param("state") boolean state);
}
