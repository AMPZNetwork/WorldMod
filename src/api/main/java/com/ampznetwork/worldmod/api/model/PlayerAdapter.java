package com.ampznetwork.worldmod.api.model;

import org.comroid.api.data.Vector;

import java.util.UUID;

public interface PlayerAdapter {
    String getName(UUID playerId);
    Vector.N3 getPosition(UUID playerId);
}
