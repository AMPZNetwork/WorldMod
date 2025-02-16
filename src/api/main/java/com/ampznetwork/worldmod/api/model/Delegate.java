package com.ampznetwork.worldmod.api.model;

import com.ampznetwork.worldmod.api.model.query.IWorldQuery;
import org.comroid.annotations.Instance;

import java.util.ServiceLoader;

public abstract class Delegate {
    public static final @Instance Delegate INSTANCE = ServiceLoader.load(Delegate.class).iterator().next();

    public abstract IWorldQuery parseQuery(String query);
}
