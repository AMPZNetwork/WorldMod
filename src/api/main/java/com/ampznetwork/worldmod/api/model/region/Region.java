package com.ampznetwork.worldmod.api.model.region;

import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.model.EntityType;
import com.ampznetwork.libmod.api.util.NameGenerator;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.mini.PointCollider;
import com.ampznetwork.worldmod.api.model.mini.Prioritized;
import com.ampznetwork.worldmod.api.model.mini.PropagationController;
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
import org.comroid.api.Polyfill;
import org.comroid.api.attr.Named;
import org.comroid.api.data.Vector;
import org.jetbrains.annotations.Nullable;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Long.*;
import static java.util.stream.Stream.*;

@Value
@Entity
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Table(name = "worldmod_regions", uniqueConstraints = @UniqueConstraint(columnNames = { "name", "worldName" }))
public class Region extends DbObject implements PropagationController, ShapeCollider, Prioritized, Named, PointCollider {
    private static final Map<String, Region>                    GlobalRegions    = new ConcurrentHashMap<>();
    public static final  EntityType<Region, Builder<Region, ?>> TYPE             = Polyfill.uncheckedCast(new EntityType<>(Region::builder,
            null,
            Region.class,
            Region.Builder.class));
    public static final  Comparator<Region>                     BY_PRIORITY      = Comparator.comparingLong(Region::getPriority).reversed();
    public static        String                                 GlobalRegionName = "#global";

    public static Region global(String worldName) {
        return GlobalRegions.computeIfAbsent(worldName,
                $ -> Region.builder()
                        .name(GlobalRegionName)
                        .worldName(worldName)
                        .priority(Long.MIN_VALUE)
                        .area(new Area(Shape.Cuboid,
                                List.of(new Vector.N4(MIN_VALUE, MIN_VALUE, MIN_VALUE, MIN_VALUE),
                                        new Vector.N4(MAX_VALUE, MAX_VALUE, MAX_VALUE, MAX_VALUE))))
                        .build());
    }

    @Default                                                                                 String          name       = NameGenerator.POI.get();
    @Default                                                                                 String          worldName  = "world";
    @Singular("owner") @ManyToMany @CollectionTable(name = "worldmod_region_owners")         Set<Player>     owners;
    @Singular("member") @ManyToMany @CollectionTable(name = "worldmod_region_members")       Set<Player>     members;
    @ElementCollection(fetch = FetchType.EAGER) @Singular @Convert(converter = Area.Converter.class) @Column(name = "area")
    @CollectionTable(name = "worldmod_region_areas", joinColumns = @JoinColumn(name = "id")) Set<Area>       areas;
    @ElementCollection(fetch = FetchType.EAGER) @Singular("flag") @Convert(converter = Flag.Usage.Converter.class) @Column(name = "flag")
    @CollectionTable(name = "worldmod_region_flags", joinColumns = @JoinColumn(name = "id")) Set<Flag.Usage> declaredFags;
    @Default @Nullable @NonFinal @ManyToOne                                                  Player          claimOwner = null;
    @Default @Nullable @NonFinal @ManyToOne                                                  Group           group      = null;
    @Default @NonFinal                                                                       long            priority   = 0;

    public Set<Player> getOwners() {
        return Stream.concat(Stream.of(claimOwner), owners.stream()).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
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
                ? declaredFags.stream()
                : concat(declaredFags.stream(), group.streamDeclaredFlags())).sorted(Comparator.<Flag.Usage>comparingLong(value -> -value.getFlag()
                .getPriority()).thenComparingLong(value -> -value.getPriority()));
    }
}
