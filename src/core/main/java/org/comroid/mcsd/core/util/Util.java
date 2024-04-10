package org.comroid.mcsd.core.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.stream.Collectors;

@UtilityClass
public class Util {
    public static String kyoriComponentString(Component it) {
        if (it instanceof TextComponent text)
            return text.content() + text.children().stream()
                    .map(Util::kyoriComponentString)
                    .collect(Collectors.joining());
        return "";
    }
}
