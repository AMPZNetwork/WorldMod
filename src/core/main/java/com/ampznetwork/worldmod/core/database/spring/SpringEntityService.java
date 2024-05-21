package com.ampznetwork.worldmod.core.database.spring;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.internal.EntityService;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import org.comroid.api.data.Vector;
import org.comroid.api.func.util.Debug;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@SpringBootApplication
@EnableJpaRepositories
@EntityScan(basePackages = {"com.ampznetwork.worldmod.api.model.region"})
public class SpringEntityService implements EntityService {
    public static SpringEntityService instance;
    static WorldMod worldMod;
    private static DatabaseType dbType;
    private static String dbUrl;
    private static String dbUsername;
    private static String dbPassword;
    public @Autowired GroupRepository groups;
    public @Autowired RegionRepository regions;

    public static SpringEntityService init(WorldMod worldMod, DatabaseType type, String dbUrl, String dbUsername, String dbPassword) {
        SpringEntityService.worldMod = worldMod;
        SpringEntityService.dbType = type;
        SpringEntityService.dbUrl = dbUrl;
        SpringEntityService.dbUsername = dbUsername;
        SpringEntityService.dbPassword = dbPassword;
        var app = new SpringApplication(SpringEntityService.class);
        app.setLogStartupInfo(Debug.isDebug());
        app.setBannerMode(Banner.Mode.OFF);
        app.setApplicationContextFactory($ -> new GenericApplicationContext(new DefaultListableBeanFactory()){
            @Override
            public ClassLoader getClassLoader() {
                return SpringEntityService.class.getClassLoader();
            }
        });
        return instance = app.run().getBean(SpringEntityService.class);
    }

    @Bean
    public DataSource db() {
        return DataSourceBuilder.create()
                .driverClassName(dbType.getDriverClass().getCanonicalName())
                .url(dbUrl)
                .username(dbUsername)
                .password(dbPassword)
                .build();
    }

    // todo

    @Override
    public Optional<Region> findRegion(String name, String worldName) {
        return Optional.empty();
    }

    @Override
    public Optional<Region> findRegion(Vector.N3 location, String worldName) {
        return Optional.empty();
    }

    @Override
    public Stream<Region> findRegions(UUID participantId) {
        return null;
    }

    @Override
    public Stream<Region> findClaims(UUID claimOwnerId) {
        return null;
    }

    @Override
    public Optional<Group> findGroup(String name) {
        return Optional.empty();
    }
}
