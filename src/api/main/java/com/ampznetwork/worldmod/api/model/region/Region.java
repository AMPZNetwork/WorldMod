package com.ampznetwork.worldmod.api.model.region;

import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.mini.PointCollider;
import com.ampznetwork.worldmod.api.model.mini.Prioritized;
import com.ampznetwork.worldmod.api.model.mini.PropagationController;
import com.ampznetwork.worldmod.api.model.mini.RegionCompositeKey;
import com.ampznetwork.worldmod.api.model.mini.ShapeCollider;
import com.ampznetwork.worldmod.api.model.sel.Area;
import com.ampznetwork.worldmod.api.model.sel.Chunk;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;
import org.comroid.api.attr.Named;
import org.comroid.api.data.Vector;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToOne;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Long.MAX_VALUE;
import static java.lang.Long.MIN_VALUE;
import static java.util.stream.Stream.concat;

@Value
@Entity
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(force = true)
@IdClass(RegionCompositeKey.class)
public class Region extends DbObject.ByPoiName implements PropagationController, ShapeCollider, Prioritized, Named, PointCollider {
    private static final Map<String, Region> GlobalRegions    = new ConcurrentHashMap<>();
    public static        String              GlobalRegionName = "#global";

    public static Region global(String worldName) {
        return GlobalRegions.computeIfAbsent(worldName,
                $ -> Region.builder()
                        .id(GlobalRegionName)
                        .worldName(worldName)
                        .priority(Long.MIN_VALUE)
                        .area(new Area(Shape.Cuboid,
                                List.of(new Vector.N4(MIN_VALUE, MIN_VALUE, MIN_VALUE, MIN_VALUE), new Vector.N4(MAX_VALUE, MAX_VALUE, MAX_VALUE, MAX_VALUE))))
                        .build());
    }

    @Id @Default                                                                                                   String          worldName  = "world";
    @ElementCollection(fetch = FetchType.EAGER) @Singular @Convert(converter = Area.Converter.class)               Set<Area>       areas;
    @ElementCollection(fetch = FetchType.EAGER) @Singular("owner")                                                 Set<UUID>       ownerIDs;
    @ElementCollection(fetch = FetchType.EAGER) @Singular("member")                                                Set<UUID>       memberIDs;
    @ElementCollection(fetch = FetchType.EAGER) @Singular("flag") @Convert(converter = Flag.Usage.Converter.class) Set<Flag.Usage> declaredFlags;
    @OneToOne @Default @Nullable @NonFinal                                                                         Group           group      = null;
    @Default @NonFinal                                                                                             long            priority   = 0;
    @Default @Nullable @NonFinal                                                                                   UUID            claimOwner = null;

    public Set<UUID> getOwnerIDs() {
        return Stream.concat(Stream.of(claimOwner), ownerIDs.stream()).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
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
                : concat(declaredFlags.stream(), group.streamDeclaredFlags())).sorted(Comparator.<Flag.Usage>comparingLong(value -> -value.getFlag()
                .getPriority()).thenComparingLong(value -> -value.getPriority()));
    }

    public RegionCompositeKey key() {
        return new RegionCompositeKey(id, worldName);
    }
}
