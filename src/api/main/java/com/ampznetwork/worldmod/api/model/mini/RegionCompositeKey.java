package com.ampznetwork.worldmod.api.model.mini;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionCompositeKey implements Serializable {
    private String name;
    private String worldName;
}
