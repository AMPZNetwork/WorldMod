package com.ampznetwork.worldmod.api.model;

import java.util.Set;
import java.util.UUID;

public interface OwnedByParty {
    Set<UUID> getOwnerIDs();
    Set<UUID> getMemberIDs();
}
