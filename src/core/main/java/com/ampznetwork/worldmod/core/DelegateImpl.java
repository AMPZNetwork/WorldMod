package com.ampznetwork.worldmod.core;

import com.ampznetwork.worldmod.api.model.Delegate;
import com.ampznetwork.worldmod.core.query.WorldQuery;

public class DelegateImpl extends Delegate {
    @Override
    public WorldQuery parseQuery(String query) {
        return WorldQuery.parse(query);
    }
}
