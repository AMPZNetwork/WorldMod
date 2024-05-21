package com.ampznetwork.worldmod.api.model.sel;

import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.mini.ShapeCollider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import lombok.*;
import org.comroid.api.data.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Data
@AllArgsConstructor
public final class Area implements ShapeCollider {
    private final Shape shape;
    private final List<Vector.N4> spatialAnchors;

    public Vector.N4[] getSpatialAnchors() {
        return spatialAnchors.stream()
                .filter(Objects::nonNull)
                .toArray(Vector.N4[]::new);
    }

    @Override
    public Stream<Chunk> streamChunks() {
        return getShape().streamChunks(getSpatialAnchors());
    }

    @Override
    public boolean isPointInside(Vector.N3 point) {
        return getShape().isPointInside(getSpatialAnchors(), point);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class Builder implements org.comroid.api.func.ext.Builder<Area> {
        private Shape shape = Shape.Cuboid;
        private List<Vector.N4> spatialAnchors = new ArrayList<>() {{
            for(int i=0;i<8;i++)add(null);
        }};

        @Override
        public Area build() {
            return new Area(shape, spatialAnchors);
        }
    }

    @Value
    @jakarta.persistence.Converter(autoApply = true)
    public static class Converter implements AttributeConverter<Area, String> {
        @Override
        @SneakyThrows
        public String convertToDatabaseColumn(Area attribute) {
            return new ObjectMapper().writeValueAsString(attribute);
        }

        @Override
        @SneakyThrows
        public Area convertToEntityAttribute(String dbData) {
            return new ObjectMapper().readValue(dbData, Area.class);
        }
    }
}