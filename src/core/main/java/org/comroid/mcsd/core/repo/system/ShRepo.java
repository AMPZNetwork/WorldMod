package org.comroid.mcsd.core.repo.system;

import org.comroid.mcsd.core.entity.AbstractEntity;
import org.comroid.mcsd.core.entity.system.ShConnection;
import org.springframework.data.repository.CrudRepository;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public interface ShRepo extends AbstractEntity.Repo<ShConnection> {
    default Map<String, UUID> toShMap() {
        return StreamSupport.stream(findAll().spliterator(), false)
                .collect(Collectors.toUnmodifiableMap(ShConnection::toString, ShConnection::getId));
    }
}
