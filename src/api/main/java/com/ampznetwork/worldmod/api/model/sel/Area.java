package com.ampznetwork.worldmod.api.model.sel;

import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.libmod.api.model.EntityType;
import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.mini.ShapeCollider;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.comroid.api.Polyfill;
import org.comroid.api.data.Vector;
import org.comroid.api.func.util.Command;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Data
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "worldmod_areas")
public final class Area extends DbObject implements ShapeCollider {
    public static final                       EntityType<Area, Area.Builder<Area, ?>> TYPE
                                                                                            = Polyfill.uncheckedCast(new EntityType<>(Area::builder,
            null,
            Area.class,
            Area.Builder.class));
    private @lombok.Builder.Default           Shape                                   shape = Shape.Cuboid;
    private @lombok.Builder.Default @Nullable Integer                                 x1    = null, y1 = null, z1 = null;
    private @lombok.Builder.Default @Nullable Integer x2 = null, y2 = null, z2 = null;
    private @lombok.Builder.Default @Nullable Integer x3 = null, y3 = null, z3 = null;
    private @lombok.Builder.Default @Nullable Integer x4 = null, y4 = null, z4 = null;
    private @lombok.Builder.Default @Nullable Integer x5 = null, y5 = null, z5 = null;
    private @lombok.Builder.Default @Nullable Integer x6 = null, y6 = null, z6 = null;
    private @lombok.Builder.Default @Nullable Integer x7 = null, y7 = null, z7 = null;
    private @lombok.Builder.Default @Nullable Integer x8 = null, y8 = null, z8 = null;

    @Transient
    public Vector.N3[] getSpatialAnchors() {
        return Stream.of(
                        new Integer[]{ x1, y1, z1 },
                        new Integer[]{ x2, y2, z2 },
                        new Integer[]{ x3, y3, z3 },
                        new Integer[]{ x4, y4, z4 },
                        new Integer[]{ x5, y5, z5 },
                        new Integer[]{ x6, y6, z6 },
                        new Integer[]{ x7, y7, z7 },
                        new Integer[]{ x8, y8, z8 })
                .filter(vec -> IntStream.range(0, 3).anyMatch(j -> vec[j] != null))
                .map(vec -> {
                    var out = new Vector.N3();
                    IntStream.range(0, 3).forEach(j -> out.set(j, Objects.requireNonNullElse(vec[j], 0)));
                    return out;
                }).toArray(Vector.N3[]::new);
    }

    private int getShapeMask() {
        var values = new Integer[]{ x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, x5, y5, z5, x6, y6, z6, x7, y7, z7, x8, y8, z8 };
        int mask   = 0;
        for (var i = 0; i < 8 * 3; i++)
            if (values[i] != null)
                mask |= 1 << i;
        return mask;
    }

    @PostLoad
    @PostUpdate
    public void validateShapeMask() {
        var minimumMask = shape.getAnchorPointMask();
        var shapeMask   = getShapeMask();
        if ((minimumMask & shapeMask) != minimumMask) {
            var missing = minimumMask & ~shapeMask;
            var sb      = new StringBuilder();
            for (var i = 0; i < 8 * 3; i++)
                if ((missing & (1 << i)) != 0) {
                    if (!sb.isEmpty()) sb.append(',');
                    sb.append(Character.toString(i % 8 + 'x')).append(i / 8);
                }
            throw new Command.Error("Invalid coordinates set; missing " + sb);
        }
    }

    @Override
    public Stream<Chunk> streamChunks() {
        return getShape().streamChunks(getSpatialAnchors());
    }

    @Override
    @Transient
    public boolean isPointInside(Vector.N3 point) {
        return getShape().isPointInside(getSpatialAnchors(), point);
    }
}