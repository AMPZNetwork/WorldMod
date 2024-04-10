package org.comroid.mcsd.core.repo.server;

import org.comroid.mcsd.core.entity.AbstractEntity;
import org.comroid.mcsd.core.entity.server.Backup;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface BackupRepo extends AbstractEntity.Repo<Backup> {
}
