package com.ampznetwork.worldmod.api.game;

import com.ampznetwork.worldmod.api.model.mini.OwnedByParty;
import com.ampznetwork.worldmod.api.model.mini.Prioritized;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kyori.adventure.util.TriState;
import org.comroid.api.attr.Described;
import org.comroid.api.attr.Named;
import org.comroid.api.data.seri.type.ValueType;
import org.comroid.api.func.util.Bitmask;
import org.comroid.api.info.Constraint;
import org.comroid.api.text.minecraft.Tellraw;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.comroid.api.data.seri.type.StandardValueType.BOOLEAN;

@Value
@EqualsAndHashCode(of = "name")
public class Flag implements Named, Described, Prioritized {
    private static final Map<String, Flag> $ = new ConcurrentHashMap<>();
    public static final Map<String,Flag> VALUES = Collections.unmodifiableMap($);
    public static final Flag Passthrough = new Flag("passthrough", Long.MAX_VALUE, BOOLEAN, "Passthrough", "Enable to force WorldMod to not handle any events", false);
    public static final Flag Build = new Flag("build", 50, BOOLEAN, "Building", "Enable to force WorldMod to not handle any events", false);
    public static final Flag Spawn = new Flag("spawn", 20, BOOLEAN, "", "", false);
    public static final Flag Spawn_Mobs = new Flag(Spawn, "mobs", 20, BOOLEAN, "", "", false);
    public static final Flag Interact = new Flag("interact", 30, BOOLEAN, "Interactions", "Enable to allow users to interact with your claim", false);
    public static final Flag Interact_ArmorStand = new Flag(Interact, "armorstand", 30, BOOLEAN, "", "", false);
    public static final Flag Interact_Fishing = new Flag(Interact, "fishing", 30, BOOLEAN, "", "", false);
    public static final Flag Interact_Harvest = new Flag(Interact, "harvest", 30, BOOLEAN, "", "", false);
    public static final Flag Interact_Shear = new Flag(Interact, "shear", 30, BOOLEAN, "", "", false);
    public static final Flag Interact_Lectern = new Flag(Interact, "lectern", 30, BOOLEAN, "", "", false);
    public static final Flag Interact_Leash = new Flag(Interact, "leash", 30, BOOLEAN, "", "", false);
    public static final Flag Damage = new Flag("damage", 20, BOOLEAN, "", "", false);
    public static final Flag Damage_ByBlock = new Flag(Damage, "byBlock", 20, BOOLEAN, "", "", false);
    public static final Flag Damage_ByEntity = new Flag(Damage, "byEntity", 20, BOOLEAN, "", "", false);
    public static final Flag Lingering = new Flag("lingering", 20, BOOLEAN, "", "", false);
    public static final Flag Lingering_Splash = new Flag(Lingering, "splash", 20, BOOLEAN, "", "", false);
    public static final Flag Lingering_Apply = new Flag(Lingering, "apply", 20, BOOLEAN, "", "", false);
    public static final Flag Use = new Flag("use", 40, BOOLEAN, "Usage", "Enable to allow users to use items in your claim", false);
    public static final Flag Use_Egg = new Flag(Use, "egg", 40, BOOLEAN, "", "", false);
    public static final Flag Chat = new Flag("chat", 40, BOOLEAN, "", "", false);
    public static final Flag Chat_Send = new Flag(Chat, "send", 40, BOOLEAN, "", "", false);
    public static final Flag Chat_Command = new Flag(Chat, "command", 40, BOOLEAN, "", "", false);
    public static final Flag Move = new Flag("move", 20, BOOLEAN, "", "", false);
    public static final Flag Teleport = new Flag("move", 20, BOOLEAN, "", "", false);
    public static final Flag Join = new Flag("join", 20, BOOLEAN, "", "", false);
    public static final Flag Respawn = new Flag("respawn", 20, BOOLEAN, "", "", false);
    public static final Flag Sleep = new Flag("sleep", 20, BOOLEAN, "", "", false);
    public static final Flag Craft = new Flag("craft", 20, BOOLEAN, "Craft Item", "Enable to allow users to craft specific items in your claim", false);
    public static final Flag Portal = new Flag("portal", 20, BOOLEAN, "", "", false);
    public static final Flag Pickup = new Flag("pickup", 20, BOOLEAN, "", "", false);
    public static final Flag Pickup_Arrow = new Flag(Pickup, "arrow", 20, BOOLEAN, "", "", false);
    public static final Flag Fire = new Flag("fire", 20, BOOLEAN, "", "", false);
    public static final Flag Fire_Spread = new Flag(Fire, "spread", 20, BOOLEAN, "", "", false);
    public static final Flag Fire_Damage = new Flag(Fire, "damage", 20, BOOLEAN, "", "", false);
    public static final Flag Drop = new Flag("drop", 20, BOOLEAN, "", "", false);
    public static final Flag Fade = new Flag("fade", 20, BOOLEAN, "", "", false);
    public static final Flag Form = new Flag("form", 20, BOOLEAN, "", "", false);
    public static final Flag Grow = new Flag("grow", 20, BOOLEAN, "", "", false);
    public static final Flag Shear = new Flag("shear", 20, BOOLEAN, "", "", false);
    public static final Flag Cook = new Flag("cook", 20, BOOLEAN, "", "", false);
    public static final Flag Dye = new Flag("dye", 20, BOOLEAN, "", "", false);
    public static final Flag Charge = new Flag("charge", 20, BOOLEAN, "", "", false);
    public static final Flag Charge_Creeper = new Flag(Charge, "creeper", 20, BOOLEAN, "", "", false);
    public static final Flag Leash = new Flag("leash", 20, BOOLEAN, "", "", false);
    public static final Flag Combat = new Flag("combat", 20, BOOLEAN, "", "", false);
    public static final Flag Combat_Melee = new Flag(Combat, "melee", 20, BOOLEAN, "", "", false);
    public static final Flag Combat_Ranged = new Flag(Combat, "ranged", 20, BOOLEAN, "", "", false);
    public static final Flag Regrow = new Flag("regrow", 20, BOOLEAN, "", "", false);
    public static final Flag SpellCast = new Flag("spellCast", 20, BOOLEAN, "", "", false);
    public static final Flag Tame = new Flag("tame", 20, BOOLEAN, "", "", false);
    public static final Flag Target = new Flag("target", 20, BOOLEAN, "", "", false);
    public static final Flag Target_Living = new Flag(Target, "living", 20, BOOLEAN, "", "", false);
    public static final Flag Glide = new Flag("glide", 20, BOOLEAN, "", "", false);
    public static final Flag Swim = new Flag("swim", 20, BOOLEAN, "", "", false);
    public static final Flag Transform = new Flag("transform", 20, BOOLEAN, "", "", false);
    public static final Flag HorseJump = new Flag("horseJump", 20, BOOLEAN, "", "", false);
    public static final Flag Despawn = new Flag("despawn", 20, BOOLEAN, "", "", false);
    public static final Flag Splash = new Flag("splash", 20, BOOLEAN, "", "", false);
    public static final Flag MobGriefing = new Flag("mobGriefing", 20, BOOLEAN, "", "", false);
    public static final Flag Breeding = new Flag("breeding", 20, BOOLEAN, "", "", false);
    public static final Flag Combust = new Flag("combust", 20, BOOLEAN, "", "", false);
    public static final Flag Combust_ByBlock = new Flag(Combust, "byBlock", 20, BOOLEAN, "", "", false);
    public static final Flag Combust_ByEntity = new Flag(Combust, "byEntity", 20, BOOLEAN, "", "", false);
    public static final Flag Hide = new Flag("hide", 20, BOOLEAN, "", "", false);
    public static final Flag Romance = new Flag("romance", 20, BOOLEAN, "", "", false);
    public static final Flag Exhaust = new Flag("exhaust", 20, BOOLEAN, "", "", false);
    public static final Flag Regenerate = new Flag("regenerate", 20, BOOLEAN, "", "", false);
    public static final Flag Resurrect = new Flag("resurrect", 20, BOOLEAN, "", "", false);
    public static final Flag Fertilize = new Flag("fertilize", 20, BOOLEAN, "", "", false);
    public static final Flag Dispense = new Flag("dispense", 20, BOOLEAN, "", "", false);
    public static final Flag Spread = new Flag("spread", 20, BOOLEAN, "", "", false);
    public static final Flag Barter = new Flag("barter", 20, BOOLEAN, "", "", false);
    public static final Flag SlimeSplit = new Flag("slimeSplit", 20, BOOLEAN, "", "", false);
    public static final Flag Villager = new Flag("villager", 20, BOOLEAN, "", "", false);
    public static final Flag Villager_Acquire = new Flag(Villager, "acquire", 20, BOOLEAN, "", "", false);
    public static final Flag Villager_Career = new Flag(Villager, "career", 20, BOOLEAN, "", "", false);
    public static final Flag Villager_Replenish = new Flag(Villager, "replenish", 20, BOOLEAN, "", "", false);
    public static final Flag TemperatureChange = new Flag("temperatureChange", 20, BOOLEAN, "", "", false);
    public static final Flag Anger = new Flag("anger", 20, BOOLEAN, "", "", false);
    public static final Flag Explode = new Flag("explode", 20, BOOLEAN, "Explosions", "Allow or Deny any kinds of Explosions", false);
    public static final Flag Explode_Creeper = new Flag(Explode, "creeper", 20, BOOLEAN, "Creeper Explosions", "Allow or Deny Creeper Explosions", false);
    public static final Flag Enter = new Flag("enter", 70, BOOLEAN, "Enter Area", "Disable to disallow users from entering the area", false);
    public static final Flag Leave = new Flag("leave", 70, BOOLEAN, "Leave Area", "Disable to disallow users from leaving the area", false);

    @Nullable Flag parent;
    @NotNull String name;
    long priority;
    @NotNull ValueType<?> type;
    @Nullable String displayName;
    @Nullable String description;
    @Nullable Object defaultValue;

    public Flag(String name,
                long priority,
                ValueType<?> type,
                @Nullable String displayName,
                @Nullable String description,
                @NotNull Object defaultValue) {
        this(null, name, priority, type, displayName, description, defaultValue);
    }

    public Flag(@Nullable Flag parent,
                @NotNull String name,
                long priority,
                @NotNull ValueType<?> type,
                @Nullable String displayName,
                @Nullable String description,
                @Nullable Object defaultValue) {
        this.parent = parent;
        this.name = name;
        this.priority = priority;
        this.type = type;
        this.displayName = displayName;
        this.description = description;
        this.defaultValue = defaultValue;

        Constraint.decide($.containsKey(getCanonicalName()), "cached Flag '" + getBestName() + "'")
                .setExpected("nonexistent")
                .setActual("exists already")
                .invert().run();
        $.put(getCanonicalName(), this);
    }

    @Override
    public String getAlternateName() {
        return displayName;
    }

    public String getCanonicalName() {
        return parent == null ? name : parent.getCanonicalName() + '.' + name;
    }

    @Data
    @Builder
    @EqualsAndHashCode
    public static class Value implements Prioritized {
        @NotNull Flag flag;
        @NotNull TriState state;
        @Default
        @Nullable String value = null;
        @Default @Deprecated
        long target = Bitmask.combine(Value.Target.Guests, Value.Target.Members);
        @Default
        Set<Tellraw.Selector> selectors = Set.of(Tellraw.Selector.builder()
                .base(Tellraw.Selector.Base.NEAREST_PLAYER)
                .type("guest")
                .build());
        @Default
        boolean force = false;
        @Default
        long priority = 0;

        public long getPriority() {
            return force ? Long.MAX_VALUE : priority;
        }

        @Deprecated
        public boolean appliesToUser(OwnedByParty target, UUID playerId) {
            var owner = target.getOwnerIDs().contains(playerId);
            var member = target.getMemberIDs().contains(playerId);
            var mask = this.target;
            return owner ? Value.Target.Owners.isFlagSet(mask)
                    : member ? Value.Target.Members.isFlagSet(mask)
                    : Value.Target.Guests.isFlagSet(mask);
        }

        @Deprecated
        public enum Target implements Named, Bitmask.Attribute<Target> {
            Guests,
            Members,
            Owners
        }
    }
}
