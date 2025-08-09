package com.ampznetwork.worldmod.api.model.config;

import com.ampznetwork.libmod.api.model.config.ConfigurableMessageAdapter;
import com.ampznetwork.worldmod.api.model.WandType;

import java.util.Map;
import java.util.stream.Stream;

public interface WorldModConfigAdapter extends ConfigurableMessageAdapter {
    boolean isSafeMode();

    boolean chunkloadWhileOnlineOnly();

    boolean loggingSkipsNonPlayer();

    Stream<String> loggingSkipFlagNames();

    Map<WandType, String> wandItems();
}
