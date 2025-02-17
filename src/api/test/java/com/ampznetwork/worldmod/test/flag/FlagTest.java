package com.ampznetwork.worldmod.test.flag;

import com.ampznetwork.worldmod.api.flag.Flag;
import org.junit.jupiter.api.Test;

public class FlagTest {
    @Test
    public void printCanonicalNames() {
        Flag.VALUES.values().stream().map(Flag::getCanonicalName).sorted().forEach(System.out::println);
    }
}
