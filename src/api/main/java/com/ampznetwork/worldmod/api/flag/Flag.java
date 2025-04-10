package com.ampznetwork.worldmod.api.flag;

import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.worldmod.api.model.mini.OwnedByParty;
import com.ampznetwork.worldmod.api.model.mini.Prioritized;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.With;
import net.kyori.adventure.util.TriState;
import org.comroid.api.attr.Described;
import org.comroid.api.attr.Named;
import org.comroid.api.func.util.Bitmask;
import org.comroid.api.info.Constraint;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import javax.persistence.Embeddable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Value
@EqualsAndHashCode(of = { "parent", "name" })
public class Flag implements Named, Described, Prioritized {
    private static final Map<String, Flag>        $                   = new ConcurrentHashMap<>();
    public static final  Map<String, Flag>        VALUES              = Collections.unmodifiableMap($);
    public static final  Comparator<? super Flag> COMPARATOR          = Comparator.<Flag>comparingLong(Prioritized::getPriority).reversed();
    public static final  Flag                     Passthrough         = new Flag("passthrough",
            Long.MAX_VALUE,
            "Passthrough",
            "Enable to force WorldMod to not handle any events",
            false);
    public static final Flag Manage              = new Flag("manage", 10, "Manage", "Enable to allow players to manage claim", false);
    public static final Flag Chunkload           = new Flag(Manage,
            "chunkload",
            -500,
            "Chunkload",
            "Enable to keep this chunk loaded",
            false);
    public static final Flag Build               = new Flag("build",
            50,
            "Building",
            "Enable to force WorldMod to not handle any events",
            false);
    public static final Flag Place               = new Flag(Build,
            "place",
            50,
            "Placing Blocks",
            "Enable to force WorldMod to not handle any events",
            false);
    public static final Flag Break               = new Flag(Build,
            "break",
            50,
            "Breaking Blocks",
            "Enable to force WorldMod to not handle any events",
            false);
    public static final Flag Spawn               = new Flag("spawn", 20, "", "", true);
    public static final Flag Spawn_Mobs          = new Flag(Spawn, "mobs", 20, "", "", true);
    public static final  Flag                     Interact            = new Flag("interact",
            30,
            "Interactions",
            "Enable to allow users to interact with your claim",
            false);
    public static final Flag Interact_ArmorStand = new Flag(Interact, "armorstand", 30, "", "", false);
    public static final Flag Interact_Fishing    = new Flag(Interact, "fishing", 30, "", "", false);
    public static final Flag Interact_Harvest    = new Flag(Interact, "harvest", 30, "", "", false);
    public static final Flag Interact_Shear      = new Flag(Interact, "shear", 30, "", "", false);
    public static final Flag Interact_Lectern    = new Flag(Interact, "lectern", 30, "", "", false);
    public static final Flag Interact_Leash      = new Flag(Interact, "leash", 30, "", "", false);
    public static final Flag Damage              = new Flag("damage", 20, "", "", true);
    public static final Flag Damage_ByBlock      = new Flag(Damage, "byBlock", 20, "", "", true);
    public static final Flag Damage_ByEntity     = new Flag(Damage, "byEntity", 20, "", "", true);
    public static final Flag Lingering           = new Flag("lingering", 20, "", "", true);
    public static final Flag Lingering_Splash    = new Flag(Lingering, "splash", 20, "", "", true);
    public static final Flag Lingering_Apply     = new Flag(Lingering, "apply", 20, "", "", true);
    public static final Flag Use                 = new Flag("use", 40, "Usage", "Enable to allow users to use items in your claim", false);
    public static final Flag Use_Egg             = new Flag(Use, "egg", 40, "", "", false);
    public static final Flag Chat                = new Flag("chat", 40, "", "", true);
    public static final Flag Chat_Send           = new Flag(Chat, "send", 40, "", "", true);
    public static final Flag Chat_Command        = new Flag(Chat, "command", 40, "", "", true);
    public static final Flag Move                = new Flag("move", 20, "", "", true);
    public static final Flag Teleport            = new Flag("teleport", 20, "", "", true);
    public static final Flag Join                = new Flag("join", 20, "", "", true);
    public static final Flag Respawn             = new Flag("respawn", 20, "", "", true);
    public static final Flag Sleep               = new Flag("sleep", 20, "", "", true);
    public static final  Flag                     Craft               = new Flag("craft",
            20,
            "Craft Item",
            "Enable to allow users to craft specific items in your claim",
            true);
    public static final Flag Portal              = new Flag("portal", 20, "", "", true);
    public static final Flag Pickup              = new Flag("pickup", 20, "", "", true);
    public static final Flag Pickup_Arrow        = new Flag(Pickup, "arrow", 20, "", "", true);
    public static final Flag Fire                = new Flag("fire", 20, "", "", false);
    public static final Flag Fire_Spread         = new Flag(Fire, "spread", 20, "", "", false);
    public static final Flag Fire_Damage         = new Flag(Fire, "damage", 20, "", "", false);
    public static final Flag Drop                = new Flag("drop", 20, "", "", true);
    public static final Flag Fade                = new Flag("fade", 20, "", "", false);
    public static final Flag Form                = new Flag("form", 20, "", "", false);
    public static final Flag Grow                = new Flag("grow", 20, "", "", false);
    public static final Flag Shear               = new Flag("shear", 20, "", "", false);
    public static final Flag Cook                = new Flag("cook", 20, "", "", false);
    public static final Flag Dye                 = new Flag("dye", 20, "", "", false);
    public static final Flag Charge              = new Flag("charge", 20, "", "", false);
    public static final Flag Charge_Creeper      = new Flag(Charge, "creeper", 20, "", "", false);
    public static final Flag Leash               = new Flag("leash", 20, "", "", false);
    public static final Flag Combat              = new Flag("combat", 20, "", "", true);
    public static final Flag Combat_Melee        = new Flag(Combat, "melee", 20, "", "", true);
    public static final Flag Combat_Ranged       = new Flag(Combat, "ranged", 20, "", "", true);
    public static final Flag Regrow              = new Flag("regrow", 20, "", "", true);
    public static final Flag SpellCast           = new Flag("spellCast", 20, "", "", true);
    public static final Flag Tame                = new Flag("tame", 20, "", "", false);
    public static final Flag Target              = new Flag("target", 20, "", "", false);
    public static final Flag Target_Living       = new Flag(Target, "living", 20, "", "", false);
    public static final Flag Glide               = new Flag("glide", 20, "", "", true);
    public static final Flag Swim                = new Flag("swim", 20, "", "", true);
    public static final Flag Transform           = new Flag("transform", 20, "", "", false);
    public static final Flag HorseJump           = new Flag("horseJump", 20, "", "", true);
    public static final Flag Despawn             = new Flag("despawn", 20, "", "", true);
    public static final Flag Splash              = new Flag("splash", 20, "", "", true);
    public static final Flag MobGriefing         = new Flag("mobGriefing", 20, "", "", false);
    public static final Flag Breeding            = new Flag("breeding", 20, "", "", true);
    public static final Flag Combust             = new Flag("combust", 20, "", "", false);
    public static final Flag Combust_ByBlock     = new Flag(Combust, "byBlock", 20, "", "", false);
    public static final Flag Combust_ByEntity    = new Flag(Combust, "byEntity", 20, "", "", false);
    public static final Flag Hide                = new Flag("hide", 20, "", "", false);
    public static final Flag Romance             = new Flag("romance", 20, "", "", true);
    public static final Flag Exhaust             = new Flag("exhaust", 20, "", "", false);
    public static final Flag Regenerate          = new Flag("regenerate", 20, "", "", true);
    public static final Flag Resurrect           = new Flag("resurrect", 20, "", "", false);
    public static final Flag Fertilize           = new Flag("fertilize", 20, "", "", true);
    public static final Flag Dispense            = new Flag("dispense", 20, "", "", true);
    public static final Flag Spread              = new Flag("spread", 20, "", "", true);
    public static final Flag Barter              = new Flag("barter", 20, "", "", true);
    public static final Flag SlimeSplit          = new Flag("slimeSplit", 20, "", "", true);
    public static final Flag Villager            = new Flag("villager", 20, "", "", true);
    public static final Flag Villager_Acquire    = new Flag(Villager, "acquire", 20, "", "", true);
    public static final Flag Villager_Career     = new Flag(Villager, "career", 20, "", "", true);
    public static final Flag Villager_Replenish  = new Flag(Villager, "replenish", 20, "", "", true);
    public static final Flag TemperatureChange   = new Flag("temperatureChange", 20, "", "", true);
    public static final Flag Anger               = new Flag("anger", 20, "", "", true);
    public static final Flag Explode             = new Flag("explode", 20, "Explosions", "Allow or Deny any kinds of Explosions", false);
    public static final  Flag                     Explode_Creeper     = new Flag(Explode,
            "creeper",
            20,
            "Creeper Explosions",
            "Allow or Deny Creeper Explosions",
            false);
    public static final  Flag                     Enter               = new Flag("enter",
            70,
            "Enter Area",
            "Disable to disallow users from entering the area",
            true);
    public static final  Flag                     Leave               = new Flag("leave",
            70,
            "Leave Area",
            "Disable to disallow users from leaving the area",
            true);

    public static Flag getForName(String canonical) {
        var nodes = canonical.split("\\.");
        if (nodes.length == 0) throw new IllegalArgumentException(canonical);
        Flag flag = VALUES.getOrDefault(nodes[0], null);
        for (var i = 1; i < nodes.length; i++) {
            var blob = nodes[i];
            flag = flag.children.stream().filter(f -> f.name.equals(blob)).findAny().orElse(flag);
        }
        return flag;
    }

    @Nullable Flag       parent;
    @NotNull  String     name;
    @Nullable String displayName;
    @Nullable String description;
    @NotNull  List<Flag> children = new ArrayList<>();
    long    priority;
    boolean allowByDefault;

    public Flag(String name, long priority, @Nullable String displayName, @Nullable String description, boolean allowByDefault) {
        this(null, name, priority, displayName, description, allowByDefault);
    }

    public Flag(
            @Nullable Flag parent, @NotNull String name, long priority, @Nullable String displayName, @Nullable String description,
            boolean allowByDefault
    ) {
        this.parent         = parent;
        this.name           = name;
        this.priority       = priority;
        this.displayName    = displayName;
        this.description    = description;
        this.allowByDefault = allowByDefault;

        if (parent != null) parent.children.add(this);

        Constraint.decide($.containsKey(getCanonicalName()), "cached Flag '" + getBestName() + "'")
                .setExpected("nonexistent")
                .setActual("exists already")
                .invert()
                .run();
        $.put(getCanonicalName(), this);
    }

    @Override
    public String getAlternateName() {
        return displayName;
    }

    public String getCanonicalName() {
        return parent == null ? name : parent.getCanonicalName() + '.' + name;
    }

    @Override
    public String toString() {
        return getCanonicalName();
    }

    @javax.persistence.Converter(autoApply = true)
    public static class Converter implements AttributeConverter<Flag, String> {
        @Override
        @SneakyThrows
        public String convertToDatabaseColumn(Flag attribute) {
            return attribute.getCanonicalName();
        }

        @Override
        @SneakyThrows
        public Flag convertToEntityAttribute(String dbData) {
            var i    = 0;
            var keys = dbData.split("\\.");
            var current = VALUES.getOrDefault(keys[i], null);
            while (current != null && ++i < keys.length) {
                var name = keys[i];
                current = current.getChildren().stream().filter(it -> name.equals(it.getName())).findAny().orElse(null);
            }
            if (current == null) throw new RuntimeException("Unable to parse flag name: " + dbData);
            return current;
        }
    }

    @Data
    @With
    @Builder
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    @RequiredArgsConstructor
    @EqualsAndHashCode(of = { "flag", "target" })
    public static class Usage implements Prioritized {
        @Convert(converter = Flag.Converter.class) @NotNull                    Flag    flag;
        @lombok.Builder.Default @MagicConstant(valuesFromClass = Target.class) long    target = Bitmask.combine(Usage.Target.Guests, Usage.Target.Members);
        @lombok.Builder.Default @NotNull                                       TriState state    = TriState.NOT_SET;
        @lombok.Builder.Default                                                long     priority = 0;
        @lombok.Builder.Default                                                boolean forced = false;

        public long getPriority() {
            return forced ? Long.MAX_VALUE : priority;
        }

        public boolean appliesToUser(OwnedByParty target, UUID playerId) {
            var owner  = target.getOwners().stream().map(DbObject::getId).anyMatch(playerId::equals);
            var member = target.getMembers().stream().map(DbObject::getId).anyMatch(playerId::equals);
            var mask = this.target;
            return owner ? Usage.Target.Owners.isFlagSet(mask) : member ? Usage.Target.Members.isFlagSet(mask) : Usage.Target.Guests.isFlagSet(mask);
        }

        @Override
        public String toString() {
            return String.valueOf(forced ? 'f' : priority) + ' ' + flag.getCanonicalName() + " @" + target + " = " + state.name().toLowerCase();
        }

        public enum Target implements Named, Bitmask.Attribute<Usage.Target> {
            Guests, Members, Owners
        }

        @Value
        @javax.persistence.Converter(autoApply = true)
        public static class Converter implements AttributeConverter<Usage, String> {
            @Override
            @SneakyThrows
            public String convertToDatabaseColumn(Usage attribute) {
                return new ObjectMapper().writeValueAsString(attribute);
            }

            @Override
            @SneakyThrows
            public Usage convertToEntityAttribute(String dbData) {
                return new ObjectMapper().readValue(dbData, Usage.class);
            }
        }
    }
}
