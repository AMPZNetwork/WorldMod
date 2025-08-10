package com.ampznetwork.worldmod.api.model.region;

import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.model.API;
import com.ampznetwork.libmod.api.model.EntityType;
import com.ampznetwork.libmod.api.util.NameGenerator;
import com.ampznetwork.worldmod.api.flag.Flag;
import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.mini.PointCollider;
import com.ampznetwork.worldmod.api.model.mini.PropagationController;
import com.ampznetwork.worldmod.api.model.mini.ShapeCollider;
import com.ampznetwork.worldmod.api.model.query.IWorldQuery;
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
import org.comroid.api.func.util.Tuple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.CollectionTable;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Integer.*;
import static java.util.stream.Stream.*;

@Value
@Entity
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Table(name = "worldmod_regions", uniqueConstraints = @UniqueConstraint(columnNames = { "name", "worldName" }))
public class Region extends DbObject implements PropagationController, ShapeCollider, Named, PointCollider {
    private static final Map<String, Region>                    GlobalRegions      = new ConcurrentHashMap<>();
    public static final  EntityType<Region, Builder<Region, ?>> TYPE               = Polyfill.uncheckedCast(new EntityType<>(Region::builder,
            null,
            Region.class,
            Region.Builder.class));
    public static final  Comparator<Region>                     BY_PRIORITY        = Comparator.comparingLong(Region::getPriority).reversed();
    public static        String                                 GLOBAL_REGION_NAME = "#global";

    public static Region global(String worldName) {
        return GlobalRegions.computeIfAbsent(worldName,
                $ -> Region.builder()
                        .serverName("")
                        .name(GLOBAL_REGION_NAME)
                        .worldName(worldName)
                        .priority(Long.MIN_VALUE)
                        .area(Area.builder().x1(MIN_VALUE).y1(MIN_VALUE).z1(MIN_VALUE).x2(MAX_VALUE).y2(MAX_VALUE).z2(MAX_VALUE).build())
                        .build());
    }

    @NotNull                                                                           String      serverName;
    @Default                                                                           String      name      = NameGenerator.POI.get();
    @Default                                                                           String      worldName = "world";
    @Singular("owner") @ManyToMany @CollectionTable(name = "worldmod_region_owners")   Set<Player> owners;
    @Singular("member") @ManyToMany @CollectionTable(name = "worldmod_region_members") Set<Player> members;
    @Singular("query") @ElementCollection @CollectionTable(name = "worldmod_region_queries") @Convert(converter = IWorldQuery.Converter.class)
    Set<IWorldQuery> queries;
    @Singular("area") @ManyToMany @CollectionTable(name = "worldmod_region_areas") Set<Area> areas;
    @Singular @ElementCollection @Convert(converter = Flag.Converter.class)
    @CollectionTable(name = "worldmod_region_flags",
                     joinColumns = @JoinColumn(name = "id"),
                     uniqueConstraints = { @UniqueConstraint(columnNames = { "id", "flag", "state", "target" }) })
    Set<Flag.Usage> declaredFlags = new HashSet<>();
    @Default @Nullable @NonFinal @ManyToOne Player claimOwner = null;
    @Default @Nullable @NonFinal @ManyToOne Group  group      = null;
    @Default @NonFinal                      long   priority   = 0;

    @Override
    public Set<Player> getOwners() {
        return Stream.concat(Stream.of(claimOwner), owners.stream()).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Stream<Flag.Usage> streamDeclaredFlags() {
        var group = getGroup();
        return (group == null
                ? declaredFlags.stream()
                : concat(declaredFlags.stream(), group.streamDeclaredFlags())).sorted(Comparator.<Flag.Usage>comparingLong(
                value -> -value.getFlag().getPriority()).thenComparingLong(value -> -value.getPriority()));
    }

    public boolean isGlobal() {
        return GLOBAL_REGION_NAME.equals(name);
    }

    @Override
    public Stream<Chunk> streamChunks() {
        return areas.stream().flatMap(Area::streamChunks);
    }

    @Override
    public boolean isPointInside(Vector.N3 point) {
        return areas.stream().anyMatch(area -> area.isPointInside(point));
    }

    public Stream<Region> findOverlaps(API api) {
        var chunks = streamChunks().collect(Collectors.toUnmodifiableSet());
        var points = getAreas().stream()
                .map(area -> {
                    assert area.getShape() == Shape.Cuboid;

                    var vecs = area.toVectors();
                    if (vecs.length != 2) throw new IllegalArgumentException("vecs length must be 2");

                    var a   = vecs[0];
                    var b   = vecs[1];
                    var min = new Vector.N3(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
                    var max = new Vector.N3(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
                    return new Tuple.N2<>(min, max);
                })
                .flatMap(lim -> IntStream.rangeClosed((int) lim.a.getX(), (int) lim.b.getX())
                        .boxed()
                        .parallel()
                        .flatMap(x -> IntStream.rangeClosed((int) lim.a.getY(), (int) lim.b.getY())
                                .boxed()
                                .parallel()
                                .flatMap(y -> IntStream.rangeClosed((int) lim.a.getZ(), (int) lim.b.getZ()).parallel().mapToObj(z -> new Vector.N3(x, y, z)))))
                //.peek(System.out::println)
                .collect(Collectors.toUnmodifiableSet());
        return api.getEntityService()
                .getAccessor(Region.TYPE)
                .all()
                .parallel()
                .filter(Predicate.not(Region::isGlobal))
                .filter(region -> region.streamChunks().anyMatch(chunks::contains))
                .filter(region -> points.stream().anyMatch(region::isPointInside));
    }
}
