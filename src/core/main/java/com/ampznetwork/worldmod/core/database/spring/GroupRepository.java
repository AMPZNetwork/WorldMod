package com.ampznetwork.worldmod.core.database.spring;

import com.ampznetwork.worldmod.api.model.region.Group;
import org.springframework.data.repository.CrudRepository;

public interface GroupRepository extends CrudRepository<Group, String> {
}
