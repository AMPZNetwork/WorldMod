package com.ampznetwork.worldmod.api.event;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.adp.IPropagationAdapter;
import com.ampznetwork.worldmod.api.model.mini.Prioritized;
import com.ampznetwork.worldmod.api.model.region.Region;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.java.Log;
import net.kyori.adventure.util.TriState;
import org.comroid.api.attr.Named;
import org.comroid.api.data.Vector;
import org.comroid.api.func.util.Streams;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

import static com.ampznetwork.worldmod.api.game.Flag.Build;
import static com.ampznetwork.worldmod.api.game.Flag.Passthrough;
import static java.util.Comparator.comparingLong;

@Log
@Value
@NonFinal
public class EventDispatchBase {
    WorldMod worldMod;

    public Stream<? extends Region> findRegions(Vector.N3 location) {
        return Stream.concat(worldMod.getRegions().parallelStream()
                        .filter(region -> region.streamChunks().anyMatch(chunk -> chunk.isPointInside(location)))
                        .filter(region -> region.isPointInside(location))
                        .sorted(comparingLong(Prioritized::getPriority).reversed()),
                Stream.of(Region.global("world")));
    }

    public EventState dependsOnFlag(IPropagationAdapter cancellable, UUID playerId, Vector.N3 location, Flag... flagChain) {
        return dependsOnFlag(cancellable, playerId, location, Streams.OP.LogicalOr, Streams.OP.LogicalOr, flagChain);
    }

    public EventState dependsOnFlag(IPropagationAdapter adp,
                                 UUID playerId,
                                 Vector.N3 location,
                                 Streams.OP chainOp_cancel,
                                 Streams.OP chainOp_force,
                                 Flag... flagChain) {
        var iter = findRegions(location).iterator();
        boolean cancel = false, force = false;
        while (iter.hasNext()) {
            var region = iter.next();
            for (var flag : Arrays.stream(flagChain)
                    .map(flag -> region.getEffectiveFlagValueForPlayer(flag, playerId))
                    .toList()) {
                var isGlobal = Region.GlobalRegionName.equals(region.getName());
                if (isGlobal && flag.getFlag().equals(Build) && flag.getState() != TriState.FALSE)
                    continue; // exception for build flag on global region
                var state = flag.getState();
                if (state == TriState.NOT_SET)
                    continue;
                if (state == TriState.FALSE)
                    cancel = chainOp_cancel.test(cancel, true);
                else if (state == TriState.TRUE && flag.isForce())
                    force = chainOp_force.test(force, true);
            }
        }
        if (force) {
            adp.force();
            return EventState.Forced;
        } else if (cancel) {
            adp.cancel();
            return EventState.Cancelled;
        } else return EventState.Unaffected;
    }

    public boolean passthrough(Vector.N3 location) {
        return findRegions(location)
                .map(region -> region.getFlagState(Passthrough))
                .findFirst()
                .filter(state -> state == TriState.TRUE)
                .isPresent();
    }

    public void dispatchEvent(IPropagationAdapter cancellable, UUID playerId, Vector.N3 location, Flag... flagChain) {
        if (passthrough(location))
            return;
        var result = dependsOnFlag(cancellable, playerId, location, flagChain);
        log.finer(() -> "%s by %s at %s resulted in %s".formatted(cancellable, playerId, location, result));
    }

    public enum EventState implements Named {Unaffected, Cancelled, Forced}
}