package com.ampznetwork.worldmod.core;

import com.ampznetwork.libmod.api.model.delegate.PlatformDelegate;
import com.ampznetwork.libmod.api.model.info.WorldChunk;
import com.ampznetwork.worldmod.api.model.region.Region;
import lombok.Value;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
public class ChunkloadingManager {
    WorldMod$Core mod;
    Set<Region>   loaded = new HashSet<>();

    public void poll() {
        var             active          = mod.findChunkloadedRegions().collect(Collectors.toUnmodifiableSet());
        Set<WorldChunk> enable, disable = new HashSet<>();

        // collect new entries
        enable = active.stream()
                .flatMap(region -> Stream.of(region)
                        .filter(Predicate.not(loaded::contains))
                        .flatMap(Region::streamChunks)
                        .map(chunk -> new WorldChunk(region.getWorldName(), chunk.getId()))
                        .peek($ -> loaded.add(region)))
                .collect(Collectors.toUnmodifiableSet());

        // collect obsolete entries
        for (var region : loaded.toArray(Region[]::new))
            if (!active.contains(region)) {
                region.streamChunks()
                        .map(chunk -> new WorldChunk(region.getWorldName(), chunk.getId()))
                        .filter(Predicate.not(enable::contains))
                        .forEach(disable::add);
                loaded.remove(region);
            }

        // apply changes
        enable.forEach(chunk -> PlatformDelegate.INSTANCE.enableChunkloading(mod, chunk.worldName(), chunk.vec()));
        disable.forEach(chunk -> PlatformDelegate.INSTANCE.disableChunkloading(mod, chunk.worldName(), chunk.vec()));
    }
}
