package com.ampznetwork.worldmod.api.math;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.comroid.api.data.Vector;
import org.comroid.api.info.Constraint;

import static java.lang.Math.*;

@Getter
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public enum Shape {
    Cuboid(2) {
        //region anchor indices
        public static final int A = 0;
        public static final int B = 1;

        //endregion
        @Override
        public boolean isPointInside(Vector[] spatialAnchors, Vector.N3 point) {
            final var a = spatialAnchors[A];
            final var b = spatialAnchors[B];

            Constraint.equals(a.n(), 3, "Vector A Dimension").run();
            Constraint.equals(b.n(), 3, "Vector B Dimension").run();

            // AABB (Axis-Aligned Bounding Box) check
            for (int n = 0; n < 3; n++)
                if (point.get(n) < min(a.get(n), b.get(n)) || point.get(n) > max(a.get(n), b.get(n)))
                    return false;
            return true;
        }

    },
    Spherical(2) {
        @Override
        public boolean isPointInside(Vector[] spatialAnchors, Vector.N3 point) {
            var position = spatialAnchors[Position];
            var radius = spatialAnchors[Radius];
            var rn = radius.n();

            Constraint.equals(position.n(), 3, "Vector A Dimension").run();
            Constraint.Range.inside(1, 3, rn, "Vector B Dimension").run();

            for (var pn = 0; pn < 3; pn++) {
                var p = point.get(pn);
                var ri = pn == rn - 1 ? pn : 0;
                var r = radius.get(ri);
                var dist = max(p, r) - min(p, r);
                if (dist > r)
                    return false;
            }
            return true;
        }

        //region anchor indices
        public static final int Position = 0;
        public static final int Radius = 1;
        //endregion
    };

    int anchorPointCount;

    public abstract boolean isPointInside(Vector[] spatialAnchors, Vector.N3 point);
}
