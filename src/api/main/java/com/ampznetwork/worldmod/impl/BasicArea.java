package com.ampznetwork.worldmod.impl;

import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.Area;
import lombok.*;
import org.comroid.api.data.Vector;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BasicArea implements Area {
    private final Shape shape;
    private final List<Vector> spatialAnchors;

    public Vector[] getSpatialAnchors() {
        return spatialAnchors.toArray(Vector[]::new);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Builder implements org.comroid.api.func.ext.Builder<BasicArea> {
        private Shape shape = Shape.Cuboid;
        private List<Vector> spatialAnchors = new ArrayList<>();

        @Override
        public BasicArea build() {
            return new BasicArea(shape, spatialAnchors);
        }
    }
}