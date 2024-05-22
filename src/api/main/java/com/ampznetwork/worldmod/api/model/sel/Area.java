package com.ampznetwork.worldmod.api.model.sel;

import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.mini.ShapeCollider;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.comroid.api.data.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Value
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class Area implements ShapeCollider {
    @Id
    UUID id = UUID.randomUUID();
    Shape shape;
    @ElementCollection
    @Singular
    @Convert(converter = Vector.N4.Converter.class)
    Map<@NotNull Integer, Vector.N4> spatialAnchors;

    public Vector.N4[] getSpatialAnchors() {
        return spatialAnchors.values().stream()
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
}