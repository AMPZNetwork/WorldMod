package com.ampznetwork.worldmod.core.query;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.query.IQueryManager;
import com.ampznetwork.worldmod.api.model.query.IWorldQuery;
import lombok.SneakyThrows;
import lombok.Value;
import org.comroid.api.func.exc.ThrowingFunction;
import org.comroid.api.info.Log;
import org.comroid.api.io.FileHandle;
import org.comroid.api.io.TrailingCommentOmittingInputStream;
import org.comroid.api.java.ResourceLoader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@Value
public class QueryManager implements IQueryManager {
    private final static Map<String, QueryManager> instances = new ConcurrentHashMap<>();

    public static QueryManager init(WorldMod mod, String worldName) {
        return instances.computeIfAbsent(worldName, k -> new QueryManager(mod, k));
    }

    WorldMod                mod;
    String                  worldName;
    Collection<IWorldQuery> queries;

    @SneakyThrows
    public QueryManager(WorldMod mod, String worldName) {
        this.mod       = mod;
        this.worldName = worldName;

        var cfg = new FileHandle(mod.getConfigDir()).createSubDir("worlds");
        if (!cfg.mkdirs()) throw new RuntimeException("Failed to create queries base directory: " + cfg.getAbsolutePath());
        cfg = cfg.createSubFile(worldName + ".wmq");

        ResourceLoader.assertFile(QueryManager.class, "template.wmq", cfg, () -> "# Documentation: https://github.com/AMPZNetwork/WorldMod");

        try (
                var fis = new FileInputStream(cfg); var wrap = new TrailingCommentOmittingInputStream(fis); var isr = new InputStreamReader(wrap);
                var br = new BufferedReader(isr)
        ) {
            this.queries = br.lines()
                    .filter(Predicate.not(String::isBlank))
                    .map(ThrowingFunction.logging(Log.get(), WorldQuery::parse))
                    .map(IWorldQuery.class::cast)
                    .toList();
        }
    }
}
