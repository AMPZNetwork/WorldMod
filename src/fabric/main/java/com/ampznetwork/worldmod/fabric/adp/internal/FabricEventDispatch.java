package com.ampznetwork.worldmod.fabric.adp.internal;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.adp.IPropagationAdapter;
import com.ampznetwork.worldmod.core.event.EventDispatchBase;
import lombok.Value;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.comroid.api.data.Vector;
import org.jetbrains.annotations.Nullable;

@Value
public class FabricEventDispatch extends EventDispatchBase implements PlayerBlockBreakEvents.Before {
    public FabricEventDispatch(WorldMod worldMod) {
        super(worldMod);

        PlayerBlockBreakEvents.BEFORE.register(this);
    }

    // todo listen to more events and flags

    @Override
    public boolean beforeBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        return dispatchEvent(world, player, pos, Flag.Build).isCancelled();
    }

    private IPropagationAdapter.Stateful dispatchEvent(World world, PlayerEntity player, BlockPos pos, Flag... flagChain) {
        var state = new IPropagationAdapter.Stateful();
        if (player.isSpectator())
            return state;
        super.dispatchEvent(
                state,
                player.getUuid(),
                new Vector.N3(pos.getX(), pos.getY(), pos.getZ()),
                world.getRegistryKey().getValue().toString(),
                flagChain
        );
        return state;
    }
}
