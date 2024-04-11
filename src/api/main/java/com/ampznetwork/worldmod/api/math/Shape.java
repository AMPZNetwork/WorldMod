package com.ampznetwork.worldmod.api.math;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public enum Shape {
    Cuboid(2, 2),
    Spherical(2, 3);

    int minAnchorPointCount;
    int maxAnchorPointCount;
}
