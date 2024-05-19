package com.ampznetwork.worldmod.api.model.sel;

import com.ampznetwork.worldmod.api.model.mini.PointCollider;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.comroid.api.data.Vector;

@Value
@EqualsAndHashCode(of = "id")
public class Chunk implements PointCollider {
    Vector.N2 id;

    @Override
    public boolean isPointInside(Vector.N3 location) {
        var chunkX = id.getX() * 16; // Calculate the X coordinate of the chunk's corner
        var chunkZ = id.getZ() * 16; // Calculate the Z coordinate of the chunk's corner

        // Check if the location's X and Z coordinates are within the chunk's boundaries
        var insideX = location.getX() >= chunkX && location.getX() < chunkX + 16;
        var insideZ = location.getZ() >= chunkZ && location.getZ() < chunkZ + 16;

        return insideX && insideZ;
    }
}
