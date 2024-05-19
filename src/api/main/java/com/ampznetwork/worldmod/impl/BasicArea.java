package com.ampznetwork.worldmod.impl;

import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.sel.Area;
import lombok.*;
import org.comroid.api.data.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Data
@AllArgsConstructor
public class BasicArea implements Area {
    private final Shape shape;
    private final List<Vector> spatialAnchors;

    public Vector[] getSpatialAnchors() {
        return spatialAnchors.stream()
                .filter(Objects::nonNull)
                .toArray(Vector[]::new);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Builder implements org.comroid.api.func.ext.Builder<BasicArea> {
        private Shape shape = Shape.Cuboid;
        private List<Vector> spatialAnchors = new ArrayList<>() {{
            for(int i=0;i<8;i++)add(null);
        }};

        @Override
        public BasicArea build() {
            return new BasicArea(shape, spatialAnchors);
        }
    }
}