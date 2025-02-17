package com.ampznetwork.worldmod.core.query;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.query.IQueryManager;
import com.ampznetwork.worldmod.api.model.query.IWorldQuery;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.core.query.condition.impl.WorldCondition;
import lombok.SneakyThrows;
import lombok.Value;
import org.comroid.api.func.exc.ThrowingFunction;
import org.comroid.api.info.Log;
import org.comroid.api.io.FileHandle;
import org.comroid.api.io.TrailingCommentOmittingInputStream;
import org.comroid.api.java.ResourceLoader;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Value
public class QueryManager implements IQueryManager {
    public static final String INFO_COMMENT = "# Documentation: https://github.com/AMPZNetwork/WorldMod";

    WorldMod          mod;
    WorldCondition worldCondition;
    List<IWorldQuery> queries;

    @SneakyThrows
    public QueryManager(WorldMod mod, String worldName) {
        this.mod            = mod;
        this.worldCondition = new WorldCondition(ValueComparator.EQUALS, worldName);

        var cfg = new FileHandle(mod.getConfigDir()).createSubDir("worlds");
        if (!cfg.mkdirs() && !cfg.exists()) throw new RuntimeException("Failed to create queries base directory: " + cfg.getAbsolutePath());
        cfg = cfg.createSubFile(worldName + ".wmq");

        ResourceLoader.assertFile(QueryManager.class, "template.wmq", cfg, () -> INFO_COMMENT);

        try (
                var fis = new FileInputStream(cfg); var wrap = new TrailingCommentOmittingInputStream(fis); var isr = new InputStreamReader(wrap);
                var br = new BufferedReader(isr)
        ) {
            this.queries = br.lines()
                    .filter(Predicate.not(String::isBlank))
                    .map(ThrowingFunction.logging(Log.get(), WorldQuery::parse))
                    .filter(Objects::nonNull)
                    .peek(query -> {
                        var conditions = query.getConditions();
                        if (!Region.GLOBAL_REGION_NAME.equals(worldName) && !conditions.contains(worldCondition)) conditions.add(worldCondition);
                    })
                    .collect(Collectors.toList());
        }
    }

    @Override
    @SneakyThrows
    public void save() {
        var str = INFO_COMMENT + '\n' + queries.stream().map(Object::toString).collect(Collectors.joining("\n"));

        var cfg = new FileHandle(mod.getConfigDir()).createSubDir("worlds");
        if (!cfg.mkdirs() && !cfg.exists()) throw new RuntimeException("Failed to create queries base directory: " + cfg.getAbsolutePath());
        cfg = cfg.createSubFile(worldCondition.getWorlds()[0] + ".wmq");

        try (var fos = new FileOutputStream(cfg)) {
            fos.write(str.getBytes(StandardCharsets.US_ASCII));
        }
    }
}
