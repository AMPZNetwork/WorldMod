package com.ampznetwork.worldmod.api.model.region;

import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.mini.Prioritized;
import com.ampznetwork.worldmod.api.model.mini.PropagationController;
import com.ampznetwork.worldmod.api.model.mini.ShapeCollider;
import com.ampznetwork.worldmod.api.model.sel.Area;
import com.ampznetwork.worldmod.api.model.sel.Chunk;
import com.ampznetwork.worldmod.impl.BasicArea;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Singular;
import lombok.Value;
import org.comroid.api.attr.Named;
import org.comroid.api.data.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;
import static java.util.stream.Stream.concat;

@Value
@Builder
public class Region implements PropagationController, ShapeCollider, Prioritized, Named, PointCollider {
    private static final Map<String, Region> GlobalRegions = new ConcurrentHashMap<>();
    public static String GlobalRegionName = "#global";
    String name;
    @Default @Nullable Group group = null;
    @Default String worldName = "world";
    @Default long priority = 0;
    @Singular Set<Area> areas;
    @Singular Set<UUID> ownerIDs;
    @Singular Set<UUID> memberIDs;
    @Singular Set<Flag.Value> declaredFlags;

    public static Region global(String worldName) {
        return GlobalRegions.computeIfAbsent(worldName,
                $ -> Region.builder()
                        .name(GlobalRegionName)
                        .worldName(worldName)
                        .priority(Long.MIN_VALUE)
                        .area(new BasicArea(Shape.Cuboid, List.of(
                                new Vector.N3(MIN_VALUE, MIN_VALUE, MIN_VALUE),
                                new Vector.N3(MAX_VALUE, MAX_VALUE, MAX_VALUE)
                        )))
                        .build());
    }

    @Override
    public Stream<Chunk> streamChunks() {
        return areas.stream().flatMap(Area::streamChunks);
    }

    @Override
    public boolean isPointInside(Vector.N3 point) {
        return areas.stream().anyMatch(area -> area.isPointInside(point));
    }

    @Override
    public Stream<Flag.Value> streamDeclaredFlags() {
        var group = getGroup();
        return (group == null
                ? declaredFlags.stream()
                : concat(declaredFlags.stream(), group.streamDeclaredFlags()))
                .sorted(Comparator.<Flag.Value>comparingLong(value -> -value.getFlag().getPriority())
                        .thenComparingLong(value -> -value.getPriority()));
    }
}
