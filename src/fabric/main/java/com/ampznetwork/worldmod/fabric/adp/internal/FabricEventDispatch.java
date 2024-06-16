package com.ampznetwork.worldmod.fabric.adp.internal;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.adp.IPropagationAdapter;
import com.ampznetwork.worldmod.core.event.EventDispatchBase;
import lombok.Value;
import net.minecraft.util.ActionResult;
import org.comroid.api.data.Vector;
import org.comroid.api.func.util.Streams;

import java.util.UUID;

@Value
public class FabricEventDispatch extends EventDispatchBase {
    public FabricEventDispatch(WorldMod worldMod) {
        super(worldMod);
    }

    public ActionResult dependsOnFlag(UUID playerId, Vector.N3 location, String worldName, Streams.OP chainOp_cancel, Streams.OP chainOp_force, Flag... flagChain) {
        return switch (super.dependsOnFlag(IPropagationAdapter.DUMMY, playerId, location, worldName, chainOp_cancel, chainOp_force, flagChain)) {
            case Unaffected, Forced -> ActionResult.PASS;
            case Cancelled -> ActionResult.FAIL;
        };
    }
}
