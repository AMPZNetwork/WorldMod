package com.ampznetwork.worldmod.api.model.region;

import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.mini.OwnedByParty;
import com.ampznetwork.worldmod.api.model.mini.Prioritized;
import com.ampznetwork.worldmod.api.model.mini.PropagationController;
import com.ampznetwork.worldmod.api.model.sel.Area;
import com.ampznetwork.worldmod.api.model.sel.Chunk;
import lombok.Value;
import org.comroid.api.attr.Named;
import org.comroid.api.data.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

@Value
public class Region implements Area, PropagationController, Prioritized, Named {
    private static final Map<String, Region> GlobalRegions = new ConcurrentHashMap<>();
    public static String GlobalRegionName = "#global";
    String name;
    @Nullable Group group;
    String worldName;
    long priority;
    Shape shape;
    Vector[] spatialAnchors;
    Set<UUID> ownerIDs = new HashSet<>();
    Set<UUID> memberIDs = new HashSet<>();
    Set<Flag.Value> declaredFlags = new HashSet<>();

    public static Region global(String worldName) {
        return GlobalRegions.computeIfAbsent(worldName,
                $ -> new Region(GlobalRegionName, null, worldName, Long.MIN_VALUE, Shape.Cuboid, new Vector[]{
                        new Vector.N3(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE),
                        new Vector.N3(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE)
                }));
    }

    public Stream<Chunk> streamChunks() {
        return shape.streamChunks(spatialAnchors);
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
