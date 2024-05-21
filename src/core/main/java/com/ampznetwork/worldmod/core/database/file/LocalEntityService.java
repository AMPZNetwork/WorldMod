package com.ampznetwork.worldmod.core.database.file;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.internal.EntityService;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import lombok.Value;
import org.comroid.api.data.Vector;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Value
public class LocalEntityService implements EntityService {
    WorldMod worldMod;

    // todo

    @Override
    public Optional<Region> findRegion(String name, String worldName) {
        return Optional.empty();
    }

    @Override
    public Optional<Region> findRegion(Vector.N3 location, String worldName) {
        return Optional.empty();
    }

    @Override
    public Stream<Region> findRegions(UUID participantId) {
        return null;
    }

    @Override
    public Stream<Region> findClaims(UUID claimOwnerId) {
        return null;
    }

    @Override
    public Optional<Group> findGroup(String name) {
        return Optional.empty();
    }
}
