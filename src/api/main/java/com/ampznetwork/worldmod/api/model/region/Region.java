package com.ampznetwork.worldmod.api.model.region;

import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.mini.OwnedByParty;
import com.ampznetwork.worldmod.api.model.mini.Prioritized;
import com.ampznetwork.worldmod.api.model.sel.Area;
import com.ampznetwork.worldmod.api.model.sel.Chunk;
import lombok.Value;
import org.comroid.api.attr.Named;
import org.comroid.api.data.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

@Value
public class Region implements Area, OwnedByParty, Prioritized, FlagContainer, Named {
    String name;
    @Nullable Group group;
    long priority;
    Shape shape;
    Vector[] spatialAnchors;
    Set<UUID> ownerIDs = new HashSet<>();
    Set<UUID> memberIDs = new HashSet<>();
    Set<Flag.Value> declaredFlags = new HashSet<>();

    public Stream<Chunk> streamChunks() {
    }

    @Override
    public Stream<Flag.Value> streamDeclaredFlags() {
        var group = getGroup();
        return (group == null
                ? declaredFlags.stream()
                : concat(declaredFlags.stream(), group.streamDeclaredFlags()))
                .sorted(Comparator.<Flag.Value>comparingLong(value -> -value.flag().getPriority())
                        .thenComparingLong(value -> -value.getPriority()));
    }
}
