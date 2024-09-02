package com.ampznetwork.worldmod.api.model.sel;

import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.mini.ShapeCollider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import org.comroid.api.data.Vector;

import javax.persistence.AttributeConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.comroid.api.data.seri.adp.Jackson.*;

@Data
@AllArgsConstructor
public final class Area implements ShapeCollider {
    private final Shape shape;
    private final List<Vector.N3> spatialAnchors;

    public Vector.N3[] getSpatialAnchors() {
        return spatialAnchors.stream()
                .filter(Objects::nonNull)
                .toArray(Vector.N3[]::new);
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
        private Shape           shape          = Shape.Cuboid;
        private List<Vector.N3> spatialAnchors = new ArrayList<>() {{
            for (int i = 0; i < 8; i++) add(null);
        }};

        @Override
        public Area build() {
            return new Area(shape, spatialAnchors);
        }
    }

    @Value
    @javax.persistence.Converter(autoApply = true)
    public static class Converter implements AttributeConverter<Area, String> {
        @Override
        @SneakyThrows
        public String convertToDatabaseColumn(Area attribute) {
            var obj = JSON.createObjectNode().asObject();

            // shape
            obj.set("shape", attribute.shape.name());

            // anchors
            var anchors = JSON.createArrayNode().asArray();
            for (var anchor : attribute.spatialAnchors) {
                if (anchor == null)
                    continue;
                var point = JSON.createObjectNode().asObject();
                // x y z
                point.set("x", anchor.getX());
                point.set("y", anchor.getY());
                point.set("z", anchor.getZ());
                anchors.add(point);
            }
            obj.put("anchors", anchors);

            return obj.toSerializedString();
        }

        @Override
        @SneakyThrows
        public Area convertToEntityAttribute(String dbData) {
            var obj = Objects.requireNonNull(JSON.parse(dbData), "Unable to parse data: " + dbData).asObject();
            var it  = new Builder();

            // shape
            it.setShape(Shape.valueOf(obj.get("shape").asString()));

            // anchors
            var anchors = new ArrayList<Vector.N3>();
            for (var anchor : obj.get("anchors").asArray()) {
                var point = new Vector.N3();
                // x y z
                point.setX(anchor.get("x").asDouble());
                point.setY(anchor.get("y").asDouble());
                point.setZ(anchor.get("z").asDouble());
                anchors.add(point);
            }
            it.setSpatialAnchors(anchors);

            return it.build();
        }
    }
}