package com.ampznetwork.worldmod.api.model.mini;

import com.ampznetwork.libmod.api.entity.DbObject;
import lombok.Data;

import java.io.Serializable;

@Data
@SuppressWarnings("unused")
public class RegionCompositeKey extends DbObject.CompositeKey<String, String> implements Serializable {
    public RegionCompositeKey() {
    }

    public RegionCompositeKey(String name, String worldName) {
        super(name, worldName);
    }
}
