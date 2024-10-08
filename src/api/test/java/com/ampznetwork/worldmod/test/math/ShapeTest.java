package com.ampznetwork.worldmod.test.math;

import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.sel.Chunk;
import org.comroid.api.data.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ShapeTest {
    @Test
    public void testStreamChunksCuboid() {
        // -117 146 78 (-8 4)
        //  -71 118 42 (-5 2)

        final var control = IntStream.range(-7, -3).boxed()
                .flatMap(x -> Stream.of(
                        new Chunk(new Vector.N2(x, 2)),
                        new Chunk(new Vector.N2(x, 3)),
                        new Chunk(new Vector.N2(x, 4))))
                .collect(Collectors.toList());
        final var anchors = new Vector.N3[]{
                new Vector.N3(-117, 146, 78),
                new Vector.N3(-71, 118, 42)
        };
        final var result = Shape.Cuboid.streamChunks(anchors).toList();

        Assertions.assertTrue(result.stream().allMatch(control::remove), "Too many or invalid chunks streamed");
        Assertions.assertTrue(control.isEmpty(), "Not enough chunks streamed");
    }
}
