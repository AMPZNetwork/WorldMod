package com.ampznetwork.worldmod.api.math;

import com.ampznetwork.worldmod.api.model.sel.Chunk;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.comroid.api.data.Vector;
import org.comroid.api.func.util.Streams;
import org.comroid.api.info.Constraint;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.max;
import static java.lang.Math.min;

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

            //Constraint.equals(a.n(), 3, "Vector A Dimension").run();
            //Constraint.equals(b.n(), 3, "Vector B Dimension").run();

            // AABB (Axis-Aligned Bounding Box) check
            for (int n = 0; n < 3; n++)
                if (point.get(n) < min(a.get(n), b.get(n)) || point.get(n) > max(a.get(n), b.get(n)))
                    return false;
            return true;
        }

        @Override
        public Stream<Chunk> streamChunks(Vector[] spatialAnchors) {
            final var a = spatialAnchors[A];
            final var b = spatialAnchors[B];

            Constraint.equals(a.n(), 3, "Vector A Dimension").run();
            Constraint.equals(b.n(), 3, "Vector B Dimension").run();

            // a to b chunk generation
            var min = new Vector.N2(min(a.getX(), b.getX()), min(a.getZ(), b.getZ()));
            var max = new Vector.N2(max(a.getX(), b.getX()), max(a.getZ(), b.getZ()));

            // todo: test
            return IntStream.range((int) min.getX(), (int) max.getX())
                    .map(x -> x / 16)
                    .boxed()
                    .map(x -> (double) x)
                    .flatMap(x -> IntStream.range((int) min.getY(), (int) max.getY())
                            .map(z -> z / 16)
                            .mapToObj(z -> new Chunk(new Vector.N2(x, z))))
                    .distinct();
        }

    }/*,
    Spherical(2) {
        @Override
        public boolean isPointInside(Vector[] spatialAnchors, Vector.N3 point) {
            var position = spatialAnchors[Position];
            var radius = spatialAnchors[Radius];
            var rn = radius.n();

            Constraint.equals(position.n(), 3, "Position Dimension").run();
            Constraint.Range.inside(1, 3, rn, "Radius Dimension").run();

            // todo fixme this is wrong dumbass
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

        @Override
        public Stream<Chunk> streamChunks(Vector[] spatialAnchors) {
            var position = spatialAnchors[Position];
            var radius = spatialAnchors[Radius];

            Constraint.equals(position.n(), 3, "Position Dimension").run();

            // todo: test
            var cr = radius.stream().max().orElseThrow() / 16;
            var c = position.divi(16);
            return IntStream.range((int) -cr, (int) cr)
                    .boxed()
                    .map(x -> (double) x)
                    .flatMap(delta -> Stream.of(c.addi(delta), c.subi(delta)))
                    .collect(Streams.append(c))
                    .distinct()
                    .flatMap(Streams.cast(Vector.N2.class))
                    .map(Chunk::new);
        }

        //region anchor indices
        public static final int Position = 0;
        public static final int Radius = 1;
        //endregion
    }*/;

    int anchorPointCount;

    public abstract boolean isPointInside(Vector[] spatialAnchors, Vector.N3 point);

    public abstract Stream<Chunk> streamChunks(Vector[] spatialAnchors);
}
