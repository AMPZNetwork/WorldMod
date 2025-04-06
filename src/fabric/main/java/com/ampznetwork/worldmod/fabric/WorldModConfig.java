package com.ampznetwork.worldmod.fabric;

import com.ampznetwork.worldmod.api.WorldMod;
import jdk.jfr.Name;
import lombok.Data;
import org.comroid.api.data.seri.DataNode;

import java.util.ArrayList;
import java.util.List;

@Data
@Name(value = WorldMod.AddonId)
public class WorldModConfig implements DataNode {
    boolean      loggingSkipsNonPlayer = true;
    List<String> loggingSkipFlags      = new ArrayList<>();
}
