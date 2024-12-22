package com.ampznetwork.worldmod.fabric.adp.internal;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.adp.IPropagationAdapter;
import com.ampznetwork.worldmod.core.event.EventDispatchBase;
import com.ampznetwork.worldmod.fabric.WorldModFabric;
import lombok.Value;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.comroid.api.data.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Value
public class FabricEventDispatch extends EventDispatchBase implements PlayerBlockBreakEvents.Before {
    public FabricEventDispatch(WorldMod worldMod) {
        super(worldMod);

        PlayerBlockBreakEvents.BEFORE.register(this);
    }

    // todo listen to more events and flags

    @Override
    public boolean beforeBlockBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
        return dispatchEvent(world, player, state.getBlock().getTranslationKey(), pos, Flag.Build).isCancelled();
    }

    private IPropagationAdapter.Stateful dispatchEvent(World world, Object source, Object target, BlockPos pos, Flag flag) {
        var state = new IPropagationAdapter.Stateful();
        var player = tryGetAsPlayer(source);
        if (player.isSpectator())
            return state;
        super.dispatchEvent(
                state,
                source, target,
                new Vector.N3(pos.getX(), pos.getY(), pos.getZ()),
                world.getRegistryKey().getValue().toString(),
                flag
        );
        return state;
    }

    private @Nullable PlayerEntity tryGetAsPlayer(@Nullable Object source) {
        var playerManager = ((WorldModFabric) getMod()).getServer().getPlayerManager();
        if (source instanceof PlayerEntity entity) return entity;
        else if (source instanceof UUID id) return playerManager.getPlayer(id);
        else if (source instanceof String name) return playerManager.getPlayer(name);
        else return null;
    }
}
