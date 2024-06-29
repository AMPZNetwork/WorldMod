package com.ampznetwork.worldmod.api.model.adp;

import org.comroid.api.data.Vector;
import org.comroid.api.net.REST;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerAdapter {
    default UUID getId(String name) {
        return REST.get("https://api.mojang.com/users/profiles/minecraft/" + name)
                .thenApply(rsp -> rsp.getBody().get("id").asString())
                .thenApply(UUID::fromString)
                .join();
    }

    String getName(UUID playerId);
    boolean isOnline(UUID playerId);
    Vector.N3 getPosition(UUID playerId);

    String getWorldName(UUID playerId);

    void openBook(UUID playerId, BookAdapter book);

    CompletableFuture<String> anvilTextInput(UUID playerId, String title, @Nullable String value);
}
