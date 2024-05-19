package com.ampznetwork.worldmod.api.model.region;

import org.comroid.api.data.Vector;

public interface PointCollider {
    boolean isPointInside(Vector.N3 point);
}
