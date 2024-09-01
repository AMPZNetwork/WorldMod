package com.ampznetwork.worldmod.core.event;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.adp.IPropagationAdapter;
import com.ampznetwork.worldmod.api.model.region.Region;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.extern.java.Log;
import net.kyori.adventure.util.TriState;
import org.comroid.api.attr.Named;
import org.comroid.api.data.Vector;
import org.comroid.api.func.util.Streams;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.ampznetwork.worldmod.api.game.Flag.*;

@Log
@Value
@NonFinal
public class EventDispatchBase {
    WorldMod worldMod;

    @Deprecated(forRemoval = true)
    public Stream<? extends Region> findRegions(Vector.N3 location) {
        return findRegions(location, "world");
    }

    public Stream<? extends Region> findRegions(@NotNull Vector.N3 location, @NotNull String worldName) {
        return Stream.concat(
                worldMod.getEntityService().getAccessor(Region.TYPE)
                        .querySelect("select r.* from REGIONS ", Map.of("location", location, "worldname", worldName)),
                Stream.of(Region.global("world")));
    }

    public EventState dependsOnFlag(IPropagationAdapter cancellable, UUID playerId, Vector.N3 location, String worldName, Flag... flagChain) {
        return dependsOnFlag(cancellable, playerId, location, worldName, Streams.OP.LogicalOr, Streams.OP.LogicalOr, flagChain);
    }

    public EventState dependsOnFlag(
            IPropagationAdapter adp,
            UUID playerId,
            Vector.N3 location,
            String worldName,
            Streams.OP chainOp_cancel,
            Streams.OP chainOp_force,
            Flag... flagChain
    ) {
        var iter = findRegions(location, worldName).iterator();
        boolean cancel = false, force = false;
        while (iter.hasNext()) {
            var region = iter.next();
            for (var flag : Arrays.stream(flagChain)
                    .map(flag -> region.getEffectiveFlagValueForPlayer(flag, playerId))
                    .toList()) {
                var isGlobal = Region.GlobalRegionName.equals(region.getId());
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

    public boolean passthrough(Vector.N3 location, String worldName) {
        return findRegions(location, worldName)
                .map(region -> region.getFlagState(Passthrough))
                .findFirst()
                .filter(state -> state == TriState.TRUE)
                .isPresent();
    }

    public void dispatchEvent(IPropagationAdapter cancellable, UUID playerId, Vector.N3 location, String worldName, Flag... flagChain) {
        if (passthrough(location, worldName))
            return;
        var result = dependsOnFlag(cancellable, playerId, location, worldName, flagChain);
        log.finer(() -> "%s by %s at %s resulted in %s".formatted(cancellable, playerId, location, result));
    }

    public enum EventState implements Named {Unaffected, Cancelled, Forced}
}
