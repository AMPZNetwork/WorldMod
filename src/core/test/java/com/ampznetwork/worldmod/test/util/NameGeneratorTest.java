package com.ampznetwork.worldmod.test.util;

import com.ampznetwork.worldmod.core.util.NameGenerator;
import org.junit.Test;

public class NameGeneratorTest {
    @Test
    public void testGenerateName() {
        System.out.println(NameGenerator.INSTANCE.get());
    }
}
