package com.ampznetwork.worldmod.spigot.adp.internal;

import com.ampznetwork.worldmod.api.model.adp.IPropagationAdapter;
import lombok.Value;
import org.bukkit.event.Cancellable;

@Value
public class SpigotPropagationAdapter implements IPropagationAdapter {
    Cancellable cancellable;

    @Override
    public void cancel() {
        cancellable.setCancelled(true);
    }

    @Override
    public void force() {
        cancellable.setCancelled(false);
    }
}
