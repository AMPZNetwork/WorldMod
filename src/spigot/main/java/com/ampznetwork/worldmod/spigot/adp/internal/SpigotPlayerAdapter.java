package com.ampznetwork.worldmod.spigot.adp.internal;

import com.ampznetwork.worldmod.api.model.adp.BookAdapter;
import com.ampznetwork.worldmod.api.model.adp.PlayerAdapter;
import com.ampznetwork.worldmod.spigot.WorldMod$Spigot;
import lombok.Value;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.comroid.api.data.Vector;
import org.comroid.api.info.Constraint;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer.get;

@Value
public class SpigotPlayerAdapter implements PlayerAdapter, Listener {
    WorldMod$Spigot worldMod;

    @Override
    public String getName(UUID playerId) {
        return worldMod.getServer().getOfflinePlayer(playerId).getName();
    }

    @Override
    public boolean isOnline(UUID playerId) {
        return worldMod.getServer().getPlayer(playerId) != null;
    }

    @Override
    public Vector.N3 getPosition(UUID playerId) {
        var player = worldMod.getServer().getPlayer(playerId);
        Constraint.notNull(player, "player").run();
        var location = player.getLocation();
        return new Vector.N3(location.getX(), location.getY(), location.getZ());
    }

    @Override
    public String getWorldName(UUID playerId) {
        return worldMod.getServer().getPlayer(playerId).getWorld().getName();
    }

    private static final Map<AnvilQueueKey, CompletableFuture<String>> anvilQueue = new ConcurrentHashMap<>();

    @Override
    public void openBook(UUID playerId, BookAdapter book) {
        if (!isOnline(playerId))
            throw new AssertionError("Target player is not online");
        var stack = new ItemStack(Material.WRITTEN_BOOK, 1);
        var meta = Objects.requireNonNull((BookMeta) stack.getItemMeta(), "book meta");
        meta.setTitle(BookAdapter.TITLE);
        meta.setAuthor(BookAdapter.AUTHOR);
        meta.spigot().setPages(book.getPages().stream()
                .map(page -> Arrays.stream(page)
                        .map(component -> get().serialize(component))
                        .flatMap(Arrays::stream)
                        .toArray(BaseComponent[]::new))
                .toList());
        stack.setItemMeta(meta);
        Objects.requireNonNull(worldMod.getServer().getPlayer(playerId), "target player")
                .openBook(stack);
    }

    @Override
    public CompletableFuture<String> anvilTextInput(UUID playerId, String title, @Nullable String value) {
        // prepare anvil inventory
        var anvil = Bukkit.createInventory(null, InventoryType.ANVIL, title);
        var label = new ItemStack(Material.NAME_TAG, 1);
        if (value == null)
            value = "";
        var meta = Objects.requireNonNull(label.getItemMeta(), "item meta");
        meta.setDisplayName(value);
        label.setItemMeta(meta);
        anvil.setItem(0, label);

        // prepare callback
        var future = new CompletableFuture<String>();
        anvilQueue.put(new AnvilQueueKey(playerId, anvil), future);
        Objects.requireNonNull(worldMod.getServer().getPlayer(playerId), "target player")
                .openInventory(anvil);
        return future;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAnvilInventoryClick(InventoryClickEvent ice) {
        if (ice.getRawSlot() != 2 || ice.getInventory().getType() != InventoryType.ANVIL)
            return;
        var result = anvilQueue.entrySet().stream()
                .filter(e -> e.getKey().inventory.equals(ice.getClickedInventory()))
                .findAny();
        if (result.isEmpty())
            return;
        var playerId = result
                .map(Map.Entry::getKey)
                .map(AnvilQueueKey::playerId)
                .orElseThrow();
        var callback = result
                .map(Map.Entry::getValue)
                .orElseThrow();
        var item = ice.getCurrentItem();
        if (item == null)
            return;
        var meta = item.getItemMeta();
        System.out.println(meta);
        if (meta == null || !meta.hasDisplayName())
            return;
        Objects.requireNonNull(worldMod.getServer().getPlayer(playerId), "target player")
                .closeInventory();
        callback.complete(meta.getDisplayName());
    }

    private record AnvilQueueKey(UUID playerId, Inventory inventory) {
    }
}
