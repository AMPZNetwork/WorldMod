package com.ampznetwork.worldmod.core.database.file;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.internal.EntityService;
import lombok.Value;

@Value
public class LocalEntityService implements EntityService {
    WorldMod worldMod;
}
