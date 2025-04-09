package com.ampznetwork.worldmod.api.model.delegate;

import com.ampznetwork.worldmod.api.model.query.IWorldQuery;
import org.comroid.annotations.Instance;

import java.util.ServiceLoader;

public abstract class ModDelegate {
    public static final @Instance ModDelegate INSTANCE = ServiceLoader.load(ModDelegate.class).iterator().next();

    public abstract IWorldQuery parseQuery(String query);
}
