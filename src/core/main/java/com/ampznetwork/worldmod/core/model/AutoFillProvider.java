package com.ampznetwork.worldmod.core.model;

import com.ampznetwork.libmod.api.SubMod;
import com.ampznetwork.worldmod.api.flag.Flag;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import org.comroid.annotations.Instance;
import org.comroid.api.attr.Named;
import org.comroid.api.func.util.Command;
import org.comroid.api.func.util.Streams;

import java.util.stream.Stream;

import static org.comroid.api.Polyfill.*;

public interface AutoFillProvider {
    enum RegionsAndGroups implements Command.AutoFillProvider {
        @Instance INSTANCE;

        @Override
        public Stream<String> autoFill(Command.Usage usage, String argName, String currentValue) {
            return usage.getContext()
                    .stream()
                    .flatMap(Streams.cast(SubMod.class))
                    .flatMap(mod -> Stream.of(Region.TYPE, Group.TYPE).flatMap(type -> mod.getEntityService().getAccessor(uncheckedCast(type)).all()))
                    .flatMap(Streams.cast(Named.class))
                    .map(Named::getName);
        }
    }

    enum Flags implements Command.AutoFillProvider {
        @Instance INSTANCE;

        @Override
        public Stream<String> autoFill(Command.Usage usage, String argName, String currentValue) {
            return Flag.VALUES.keySet().stream().map(str -> str.contains(".") ? str.substring(0, str.indexOf(".") + 1) : str);
        }
    }
}
