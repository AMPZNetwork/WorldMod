package com.ampznetwork.worldmod.api.model.mini;

import com.ampznetwork.worldmod.api.model.region.PointCollider;
import com.ampznetwork.worldmod.api.model.sel.Chunk;

import java.util.stream.Stream;

public interface ShapeCollider extends PointCollider {
    Stream<Chunk> streamChunks();
}
