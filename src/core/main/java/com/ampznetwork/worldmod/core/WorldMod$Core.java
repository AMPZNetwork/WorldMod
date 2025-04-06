package com.ampznetwork.worldmod.core;

import com.ampznetwork.worldmod.api.WorldMod;
import lombok.SneakyThrows;
import org.comroid.api.java.ResourceLoader;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public interface WorldMod$Core extends WorldMod {
    @Override
    @SneakyThrows
    default Properties getMessages() {
        var path = "plugins/WorldMod/messages.lang";
        ResourceLoader.assertFile(WorldModCommands.class, "messages.lang", new File(path), () -> "# Include custom messages here");
        var prop = new Properties();
        try (var fis = new FileInputStream(path)) {
            prop.load(fis);
        }
        return prop;
    }
}
