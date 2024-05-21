package com.ampznetwork.worldmod.api.model.region;

import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.mini.*;
import com.ampznetwork.worldmod.api.model.sel.Area;
import com.ampznetwork.worldmod.api.model.sel.Chunk;
import jakarta.persistence.*;
import lombok.*;
import lombok.Builder.Default;
import org.comroid.api.attr.Named;
import org.comroid.api.data.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;
import static java.util.stream.Stream.concat;

@Value
@Entity
@AllArgsConstructor
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@IdClass(RegionCompositeKey.class)
public class Region implements PropagationController, ShapeCollider, Prioritized, Named, PointCollider {
    private static final Map<String, Region> GlobalRegions = new ConcurrentHashMap<>();
    public static String GlobalRegionName = "#global";
    @Id @Default @NotNull String name = UUID.randomUUID().toString();
    @Id @Default String worldName = "world";
    @OneToOne @Default @Nullable Group group = null;
    @Default long priority = 0;
    @Default@Nullable UUID claimOwner=null;
    @ElementCollection(fetch = FetchType.EAGER) @Singular @Convert(converter = Area.Converter.class) Set<Area> areas;
    @ElementCollection(fetch = FetchType.EAGER) @Singular("owner") Set<UUID> ownerIDs;
    @ElementCollection(fetch = FetchType.EAGER) @Singular("member") Set<UUID> memberIDs;
    @ElementCollection(fetch = FetchType.EAGER) @Singular("flag") @Convert(converter = Flag.Usage.Converter.class) Set<Flag.Usage> declaredFlags;

    public static Region global(String worldName) {
        return GlobalRegions.computeIfAbsent(worldName,
                $ -> Region.builder()
                        .name(GlobalRegionName)
                        .worldName(worldName)
                        .priority(Long.MIN_VALUE)
                        .area(new Area(Shape.Cuboid, List.of(
                                new Vector.N3(MIN_VALUE, MIN_VALUE, MIN_VALUE),
                                new Vector.N3(MAX_VALUE, MAX_VALUE, MAX_VALUE)
                        )))
                        .build());
    }

    public Set<UUID> getOwnerIDs() {
        return Stream.concat(Stream.of(claimOwner), ownerIDs.stream()).collect(Collectors.toUnmodifiableSet());
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
    public Stream<Flag.Usage> streamDeclaredFlags() {
        var group = getGroup();
        return (group == null
                ? declaredFlags.stream()
                : concat(declaredFlags.stream(), group.streamDeclaredFlags()))
                .sorted(Comparator.<Flag.Usage>comparingLong(value -> -value.getFlag().getPriority())
                        .thenComparingLong(value -> -value.getPriority()));
    }
}
