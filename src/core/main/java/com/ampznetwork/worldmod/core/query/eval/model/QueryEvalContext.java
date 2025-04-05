package com.ampznetwork.worldmod.core.query.eval.model;

import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.worldmod.api.WorldMod;
import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Value
public class QueryEvalContext {
    WorldMod mod;
    @Nullable       Player         player;
    @With @Nullable RelativeTarget relativeTarget;
    Map<String, @NotNull Long> flagLog;
}
