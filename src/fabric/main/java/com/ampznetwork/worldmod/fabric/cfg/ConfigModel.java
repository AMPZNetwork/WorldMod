package com.ampznetwork.worldmod.fabric.cfg;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.database.EntityService;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.Nest;

@Modmenu(modId = WorldMod.AddonId)
@SuppressWarnings({"FieldMayBeFinal", "unused"})
@Config(name = "worldmod-config", wrapperName = "Config")
public class ConfigModel {
    EntityService.Type entityService = EntityService.Type.Database;
    @Nest
    Database database;

    public static class Database {
        EntityService.DatabaseType type = EntityService.DatabaseType.h2;
        String url = "jdbc:h2:file:./worldmod.h2";
        String username = "sa";
        String password = "";
    }
}
