package com.ampznetwork.worldmod.api.model.mini;

import java.io.Serializable;

public record RegionCompositeKey(String name, String worldName) implements Serializable {
}
