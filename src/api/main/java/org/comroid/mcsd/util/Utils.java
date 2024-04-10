package org.comroid.mcsd.util;

import lombok.experimental.UtilityClass;
import org.comroid.api.func.util.DelegateStream;
import org.comroid.api.func.util.Event;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class Utils {
    public static final Set<UUID> SuperAdmins = Set.of(
            UUID.fromString("48a84b30-df71-4cff-91bd-e82fd8e58c35")
    );

    @Contract("null -> null; _ -> _")
    public static String removeAnsiEscapeSequences(@Nullable String input) {
        return input == null ? null : input.replaceAll("\u001B[\\[(][?;\\d]*[=a-zA-Z]", "");
    }

    public CompletableFuture<Event<String>> waitForOutput(@NotNull Event.Bus<String> bus, @Language("RegExp") String pattern) {
        return listenForOutput(bus, pattern).listen().once();
    }

    public Event.Bus<String> listenForOutput(@NotNull Event.Bus<String> bus, @NotNull @Language("RegExp") String pattern) {
        return bus.filter(e -> DelegateStream.IO.EventKey_Output.equals(e.getKey()))
                .filterData(str -> str.contains(pattern) | str.matches(pattern));
    }

    public Event.Bus<Matcher> listenForPattern(@NotNull Event.Bus<String> bus, @NotNull Pattern pattern) {
        //noinspection Convert2MethodRef
        return bus.mapData(input -> pattern.matcher(input))
                .filterData(matcher -> matcher.matches());
    }
}
