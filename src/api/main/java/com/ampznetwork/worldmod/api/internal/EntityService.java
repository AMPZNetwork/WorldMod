package com.ampznetwork.worldmod.api.internal;

import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.comroid.api.attr.Named;
import org.comroid.api.data.Vector;
import org.comroid.api.tree.LifeCycle;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface EntityService extends LifeCycle {
    Optional<Region> findRegion(String name, String worldName);
    Optional<Region> findRegion(Vector.N3 location, String worldName);
    Stream<Region> findRegions(UUID participantId);
    Stream<Region> findClaims(UUID claimOwnerId);
    Optional<Group> findGroup(String name);

    @Getter
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
    enum DatabaseType implements Named {
        h2(org.h2.Driver.class),
        MySQL(com.mysql.jdbc.Driver.class);

        Class<?> driverClass;
    }
}
