package com.ampznetwork.worldmod.test.util;

import com.ampznetwork.libmod.api.util.NameGenerator;
import org.junit.jupiter.api.RepeatedTest;

public class NameGeneratorTest {
    @RepeatedTest(100)
    public void testGenerateName() {
        System.out.println(NameGenerator.INSTANCE.get());
    }
}
