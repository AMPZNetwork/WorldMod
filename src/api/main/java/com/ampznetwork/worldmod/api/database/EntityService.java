package com.ampznetwork.worldmod.api.database;

import com.ampznetwork.worldmod.api.model.mini.RegionCompositeKey;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.comroid.api.attr.Named;
import org.comroid.api.data.Vector;
import org.comroid.api.tree.LifeCycle;
import org.jetbrains.annotations.Contract;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface EntityService extends LifeCycle {
    default Optional<Region> findRegion(String name, String worldName) {
        return findRegion(new RegionCompositeKey(name, worldName));
    }

    Optional<Region> findRegion(RegionCompositeKey key);

    Optional<Region> findRegion(Vector.N3 location, String worldName);

    Stream<Region> findClaims(UUID claimOwnerId);

    Optional<Group> findGroup(String name);

    @Contract("!null->param1")
    <T> T save(T it);

    @Contract("!null->param1")
    <T> T refresh(T it);

    @Getter
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    enum DatabaseType implements Named {
        h2(org.h2.Driver.class),
        MySQL(com.mysql.jdbc.Driver.class);

        Class<?> driverClass;
    }
}
