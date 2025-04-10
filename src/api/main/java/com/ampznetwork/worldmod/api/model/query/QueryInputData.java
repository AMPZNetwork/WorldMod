package com.ampznetwork.worldmod.api.model.query;

import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.worldmod.api.flag.Flag;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Singular;
import lombok.Value;
import net.kyori.adventure.key.Key;
import org.comroid.api.data.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class QueryInputData {
    @NotNull String serverName;
    @NotNull            String       worldName;
    @NotNull            Instant      timestamp;
    @Nullable @Singular List<Region> regions;
    @Nullable @Default  Player       player            = null;
    @Nullable @Default  String       nonPlayerSource   = null;
    @Nullable @Default  Key          targetResourceKey = null;
    @Nullable @Default  Vector.N3    position          = null;
    @Nullable @Default  Integer      radius            = null;
    @Nullable @Default  Flag         action            = null;
    @Nullable @Default  ObjectNode   nbt               = null;
}
