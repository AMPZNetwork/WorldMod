package com.ampznetwork.worldmod.spigot.adp.internal;

import com.ampznetwork.libmod.api.model.delegate.Cancellable;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
@RequiredArgsConstructor
@Deprecated(forRemoval = true)
public class SpigotPropagationAdapter implements Cancellable {
    org.bukkit.event.Cancellable cancellable;
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
