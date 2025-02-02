package com.ampznetwork.worldmod.api.model.query;

import com.ampznetwork.libmod.api.LibMod;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.model.info.ValueAutofillOptionsProvider;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import org.comroid.api.attr.Named;
import org.comroid.api.func.util.Command;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.stream.Stream;

public enum ConditionType implements ValueAutofillOptionsProvider {
    REGION("#global") {
        @Override
        public Stream<String> autoFillValue(Command.Usage usage, String argName, LibMod mod, String value) {
            return mod.getEntityService().getAccessor(Region.TYPE).all().map(Named::getName);
        }
    }, GROUP {
        @Override
        public Stream<String> autoFillValue(Command.Usage usage, String argName, LibMod mod, String value) {
            return mod.getEntityService().getAccessor(Group.TYPE).all().map(Named::getName);
        }
    }, SOURCE("@a") {
        @Override
        public Stream<String> autoFillValue(Command.Usage usage, String argName, LibMod mod, String value) {
            return Stream.concat(
                    // player names, todo: selectors
                    mod.getPlayerAdapter().getCurrentPlayers().map(Player::getName),
                    // entity types
                    mod.getLib().entityTypes());
        }
    }, TARGET("@a") {
        @Override
        public Stream<String> autoFillValue(Command.Usage usage, String argName, LibMod mod, String value) {
            return Stream.concat(
                    // player names, todo: selectors
                    mod.getPlayerAdapter().getCurrentPlayers().map(Player::getName),
                    // material keys
                    mod.getLib().materials());
        }
    }, RADIUS(NUMERICS), WORLD {
        @Override
        public Stream<String> autoFillValue(Command.Usage usage, String argName, LibMod mod, String value) {
            return mod.getLib().worldNames();
        }
    }, SINCE {
        @Override
        public Stream<String> autoFillValue(Command.Usage usage, String argName, LibMod mod, String value) {
            return Command.AutoFillProvider.Duration.INSTANCE.autoFill(usage, argName, value);
        }
    }, TYPE, FLAG {
        @Override
        public Stream<String> autoFillValue(Command.Usage usage, String argName, LibMod mod, String value) {
            return mod.sub(WorldMod.class).flagNames();
        }
    }, TAG, X(NUMERICS, "~"), Y(NUMERICS, "~"), Z(NUMERICS, "~"), MESSAGE {
        @Override
        public Stream<String> autoFillValue(Command.Usage usage, String argName, LibMod mod, String value) {
            return mod.sub(WorldMod.class).getMessages().keySet().stream().map(String::valueOf);
        }
    };
    private final @Nullable ValueAutofillOptionsProvider delegate;
    private final           String[]                     constants;

    ConditionType(String... constants) {
        this(null, constants);
    }

    ConditionType(@Nullable ValueAutofillOptionsProvider delegate, String... constants) {
        this.delegate  = delegate;
        this.constants = constants;
    }

    @Override
    public Stream<String> autoFillValue(Command.Usage usage, String argName, LibMod mod, String value) {
        return Stream.concat(Arrays.stream(constants).flatMap(ValueAutofillOptionsProvider::expandDigit),
                delegate == null ? Stream.of(value) : delegate.autoFillValue(usage, argName, mod, value));
    }
}
