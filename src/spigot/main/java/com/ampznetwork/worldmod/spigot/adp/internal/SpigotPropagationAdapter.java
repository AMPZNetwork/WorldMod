package com.ampznetwork.worldmod.spigot.adp.internal;

import com.ampznetwork.worldmod.api.model.adp.IPropagationAdapter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.bukkit.event.Cancellable;

@Value
@RequiredArgsConstructor
public class SpigotPropagationAdapter implements IPropagationAdapter {
    Cancellable cancellable;
    @NonFinal boolean forced = false;

    @Override
    public boolean isCancelled() {
        return cancellable.isCancelled();
    }

    @Override
    public void cancel() {
        cancellable.setCancelled(true);
    }

    @Override
    public void force() {
        cancellable.setCancelled(false);
        forced = true;
    }
}
