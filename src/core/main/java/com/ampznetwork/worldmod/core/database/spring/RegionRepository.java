package com.ampznetwork.worldmod.core.database.spring;

import com.ampznetwork.worldmod.api.model.region.Region;
import org.springframework.data.repository.CrudRepository;

public interface RegionRepository extends CrudRepository<Region, String> {
}
