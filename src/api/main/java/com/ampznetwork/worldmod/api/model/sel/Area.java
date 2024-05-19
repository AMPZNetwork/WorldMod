package com.ampznetwork.worldmod.api.model.sel;

import com.ampznetwork.worldmod.api.math.Shape;
import org.comroid.api.data.Vector;

import java.util.stream.Stream;

public interface Area {
    Shape getShape();
    Vector[] getSpatialAnchors();

    default Stream<Chunk> streamChunks() {
        return getShape().streamChunks(getSpatialAnchors());
    }
}
