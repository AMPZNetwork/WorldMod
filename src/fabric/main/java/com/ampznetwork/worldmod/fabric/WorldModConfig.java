package com.ampznetwork.worldmod.fabric;

import com.ampznetwork.libmod.fabric.config.Config;
import com.ampznetwork.worldmod.api.WorldMod;
import jdk.jfr.Name;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Name(value = WorldMod.AddonId)
public class WorldModConfig extends Config {
    boolean      loggingSkipsNonPlayer = true;
    List<String> loggingSkipFlags      = new ArrayList<>();
}
