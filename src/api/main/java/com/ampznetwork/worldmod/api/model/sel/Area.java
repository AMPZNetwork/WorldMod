package com.ampznetwork.worldmod.api.model.sel;

import com.ampznetwork.worldmod.api.math.Shape;
import org.comroid.api.data.Vector;

public interface Area {
    Shape getShape();
    Vector[] getSpatialAnchors();
}
