package com.ampznetwork.worldmod.api.model.sel;

import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.mini.ShapeCollider;
import org.comroid.api.data.Vector;

import java.util.stream.Stream;

public interface Area extends ShapeCollider {
    Shape getShape();
    Vector[] getSpatialAnchors();

    @Override
    default Stream<Chunk> streamChunks() {
        return getShape().streamChunks(getSpatialAnchors());
    }

    @Override
    default boolean isPointInside(Vector.N3 point) {
        return getShape().isPointInside(getSpatialAnchors(), point);
    }
}
