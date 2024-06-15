package com.ampznetwork.worldmod.core.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.comroid.api.text.Capitalization;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum NameGenerator implements Supplier<String> {
    INSTANCE;

    Set<String> adverbs;
    Set<String> nouns;
    Random rng = new Random();

    @SneakyThrows
    NameGenerator() {
        try (
                var advIs = new URL("https://raw.githubusercontent.com/AMPZNetwork/WorldMod/main/src/core/main/resources/adverbs.txt").openStream();
                var advIsr = new InputStreamReader(advIs);
                var advBr = new BufferedReader(advIsr);
                var nounIs = new URL("https://raw.githubusercontent.com/AMPZNetwork/WorldMod/main/src/core/main/resources/nouns.txt").openStream();
                var nounIsr = new InputStreamReader(nounIs);
                var nounBr = new BufferedReader(nounIsr)
        ) {
            adverbs = advBr.lines().collect(Collectors.toSet());
            nouns = nounBr.lines().collect(Collectors.toSet());
        }
    }

    @Override
    public String get() {
        var adverbs = this.adverbs.toArray(String[]::new);
        var nouns = this.nouns.toArray(String[]::new);
        var name = adverbs[rng.nextInt(0, adverbs.length)] + ' ' + nouns[rng.nextInt(0, nouns.length)];
        return Capitalization.Title_Case.convert(name);
    }
}
