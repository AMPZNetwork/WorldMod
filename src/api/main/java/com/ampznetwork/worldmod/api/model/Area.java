package com.ampznetwork.worldmod.api.model;

import com.ampznetwork.worldmod.api.math.Shape;
import org.comroid.api.data.Vector;

public interface Area {
    Shape getShape();
    Vector[] getSpatialAnchors();
}
