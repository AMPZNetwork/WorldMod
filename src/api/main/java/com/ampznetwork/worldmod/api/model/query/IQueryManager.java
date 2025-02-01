package com.ampznetwork.worldmod.api.model.query;

import java.util.List;

public interface IQueryManager {
    List<IWorldQuery> getQueries();

    void save();
}
