package com.ampznetwork.worldmod.fabric.adp.internal;

import com.ampznetwork.worldmod.api.model.adp.BookAdapter;
import com.ampznetwork.worldmod.api.model.adp.PlayerAdapter;
import com.ampznetwork.worldmod.fabric.WorldMod$Fabric;
import io.netty.buffer.Unpooled;
import lombok.Value;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.comroid.api.data.Vector;
import org.comroid.api.net.REST;

import java.util.Collection;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson;

@Value
public class FabricPlayerAdapter implements PlayerAdapter {
    WorldMod$Fabric worldMod;

    @Override
    public String getName(UUID playerId) {
        return REST.get("https://sessionserver.mojang.com/session/minecraft/profile/" + playerId)
                .thenApply(REST.Response::validate2xxOK)
                .thenApply(rsp -> rsp.getBody().get("name").asString())
                .exceptionally(t -> {
                    WorldMod$Fabric.LOGGER.warn("Could not retrieve Minecraft Username for user {}", playerId, t);
                    return "Steve";
                }).join();
    }

    @Override
    public boolean isOnline(UUID playerId) {
        return worldMod.getServer().getPlayerManager()
                .getPlayer(playerId) != null;
    }

    @Override
    public Vector.N3 getPosition(UUID playerId) {
        var vec = worldMod.getServer().getPlayerManager()
                .getPlayer(playerId).getPos();
        return new Vector.N3(vec.x, vec.y, vec.z);
    }

    @Override
    public String getWorldName(UUID playerId) {
        return worldMod.getServer().getPlayerManager()
                .getPlayer(playerId).getWorld()
                .getRegistryKey().getValue()
                .toString();
    }

    @Override
    public void openBook(UUID playerId, BookAdapter book) {
        var plr = worldMod.getServer().getPlayerManager()
                .getPlayer(playerId);
        ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);

        // Set the stack's title, author, and pages
        var tag = new NbtCompound();
        tag.putString("title", BookAdapter.TITLE);
        tag.putString("author", BookAdapter.AUTHOR);

        var pages = book.getPages().stream()
                .map(page -> {
                    var text = text();
                    for (var comp : page)
                        text.append(comp);
                    return text.build();
                })
                .map(page -> NbtString.of(gson().serialize(page)))
                .collect(NbtList::new, Collection::add, Collection::addAll);

        tag.put("pages", pages);
        stack.setNbt(tag);

        // Create a PacketByteBuf and write the stack item stack to it
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeItemStack(stack);

        ServerPlayNetworking.send(plr, new Identifier("minecraft", "book_open"), buf);
    }
}
