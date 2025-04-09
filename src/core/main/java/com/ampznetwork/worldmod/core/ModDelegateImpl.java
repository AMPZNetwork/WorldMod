package com.ampznetwork.worldmod.core;

import com.ampznetwork.worldmod.api.model.delegate.ModDelegate;
import com.ampznetwork.worldmod.core.query.WorldQuery;

public class ModDelegateImpl extends ModDelegate {
    @Override
    public WorldQuery parseQuery(String query) {
        return WorldQuery.parse(query);
    }
}
