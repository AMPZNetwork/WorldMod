package com.ampznetwork.worldmod.spigot.adp.game;

import com.ampznetwork.worldmod.api.model.adp.BookAdapter;
import com.google.common.collect.Multimap;
import lombok.Data;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class SpigotBookAdapter implements BookMeta, BookAdapter {
    @Nullable
    @Override
    public String getTitle() {
        return "WorldMod Menu";
    }

    @Nullable
    @Override
    public String getAuthor() {
        return "Kaleidox @ AMPZNetwork";
    }

    @Override
    public void setAuthor(@Nullable String s) {

    }

    @NotNull
    @Override
    public String getPage(int i) {
        return "";
    }

    @NotNull
    @Override
    public List<String> getPages() {
        return List.of();
    }

    @Override
    public void setPages(@NotNull List<String> list) {

    }

    @Override
    public void setPages(@NotNull String... strings) {

    }

    @Nullable
    @Override
    public Generation getGeneration() {
        return Generation.ORIGINAL;
    }

    @Override
    public void setGeneration(@Nullable BookMeta.Generation generation) {

    }

    @Override
    public boolean hasTitle() {
        return true;
    }

    @Override
    public boolean setTitle(@Nullable String s) {
        return false;
    }

    @Override
    public boolean hasAuthor() {
        return true;
    }

    @Override
    public boolean hasGeneration() {
        return true;
    }

    @Override
    public boolean hasPages() {
        return true;
    }

    @Override
    public void setPage(int i, @NotNull String s) {

    }

    @Override
    public void addPage(@NotNull String... strings) {

    }

    @Override
    public int getPageCount() {
        return 0;
    }

    @Override
    public boolean hasDisplayName() {
        return false;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "";
    }

    @Override
    public void setDisplayName(@Nullable String s) {

    }

    @Override
    public boolean hasLocalizedName() {
        return false;
    }

    @NotNull
    @Override
    public String getLocalizedName() {
        return "";
    }

    @Override
    public void setLocalizedName(@Nullable String s) {

    }

    @Override
    public boolean hasLore() {
        return false;
    }

    @Nullable
    @Override
    public List<String> getLore() {
        return List.of();
    }

    @Override
    public void setLore(@Nullable List<String> list) {

    }

    @Override
    public boolean hasCustomModelData() {
        return false;
    }

    @Override
    public int getCustomModelData() {
        return 0;
    }

    @Override
    public void setCustomModelData(@Nullable Integer integer) {

    }

    @Override
    public boolean hasEnchants() {
        return false;
    }

    @Override
    public boolean hasEnchant(@NotNull Enchantment enchantment) {
        return false;
    }

    @Override
    public int getEnchantLevel(@NotNull Enchantment enchantment) {
        return 0;
    }

    @NotNull
    @Override
    public Map<Enchantment, Integer> getEnchants() {
        return Map.of();
    }

    @Override
    public boolean addEnchant(@NotNull Enchantment enchantment, int i, boolean b) {
        return false;
    }

    @Override
    public boolean removeEnchant(@NotNull Enchantment enchantment) {
        return false;
    }

    @Override
    public boolean hasConflictingEnchant(@NotNull Enchantment enchantment) {
        return false;
    }

    @Override
    public void addItemFlags(@NotNull ItemFlag... itemFlags) {

    }

    @Override
    public void removeItemFlags(@NotNull ItemFlag... itemFlags) {

    }

    @NotNull
    @Override
    public Set<ItemFlag> getItemFlags() {
        return Set.of();
    }

    @Override
    public boolean hasItemFlag(@NotNull ItemFlag itemFlag) {
        return false;
    }

    @Override
    public boolean isUnbreakable() {
        return false;
    }

    @Override
    public void setUnbreakable(boolean b) {

    }

    @Override
    public boolean hasAttributeModifiers() {
        return false;
    }

    @Nullable
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers() {
        return null;
    }

    @Override
    public void setAttributeModifiers(@Nullable Multimap<Attribute, AttributeModifier> multimap) {

    }

    @NotNull
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(@NotNull EquipmentSlot equipmentSlot) {
        return null;
    }

    @Nullable
    @Override
    public Collection<AttributeModifier> getAttributeModifiers(@NotNull Attribute attribute) {
        return List.of();
    }

    @Override
    public boolean addAttributeModifier(@NotNull Attribute attribute, @NotNull AttributeModifier attributeModifier) {
        return false;
    }

    @Override
    public boolean removeAttributeModifier(@NotNull Attribute attribute) {
        return false;
    }

    @Override
    public boolean removeAttributeModifier(@NotNull EquipmentSlot equipmentSlot) {
        return false;
    }

    @Override
    public boolean removeAttributeModifier(@NotNull Attribute attribute, @NotNull AttributeModifier attributeModifier) {
        return false;
    }

    @NotNull
    @Override
    public String getAsString() {
        return "";
    }

    @NotNull
    @Override
    public CustomItemTagContainer getCustomTagContainer() {
        return null;
    }

    @Override
    public void setVersion(int i) {

    }

    @NotNull
    @Override
    public BookMeta clone() {
        return null;
    }

    @NotNull
    @Override
    public Spigot spigot() {
        return null;
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        return Map.of();
    }

    @NotNull
    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return null;
    }
}
