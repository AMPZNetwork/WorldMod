package com.ampznetwork.worldmod.api.model.adp;

import org.comroid.api.data.Vector;

import java.util.UUID;

public interface PlayerAdapter {
    String getName(UUID playerId);
    boolean isOnline(UUID playerId);
    Vector.N3 getPosition(UUID playerId);
    String getWorldName(UUID playerId);
}
