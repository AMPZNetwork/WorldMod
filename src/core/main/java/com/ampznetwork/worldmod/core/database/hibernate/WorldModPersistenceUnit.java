package com.ampznetwork.worldmod.core.database.hibernate;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;
import lombok.Value;
import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.sql.DataSource;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

@Value
public class WorldModPersistenceUnit implements PersistenceUnitInfo {
    HikariDataSource dataSource;
    URL jarUrl = WorldMod.class.getProtectionDomain().getCodeSource().getLocation();
    List<String> classes = Stream.<Class<?>>of(Region.class, Group.class)
            .map(Class::getCanonicalName)
            .toList();

    @Override
    public String getPersistenceUnitName() {
        return "WorldMod";
    }

    @Override
    public String getPersistenceProviderClassName() {
        return HibernatePersistenceProvider.class.getCanonicalName();
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return PersistenceUnitTransactionType.JTA;
    }

    @Override
    public DataSource getJtaDataSource() {
        return dataSource;
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return dataSource;
    }

    @Override
    public List<String> getMappingFileNames() {
        return List.of();
    }

    @Override
    public List<URL> getJarFileUrls() {
        return List.of();
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        return jarUrl;
    }

    @Override
    public List<String> getManagedClassNames() {
        return classes;
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return true;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return SharedCacheMode.ALL;
    }

    @Override
    public ValidationMode getValidationMode() {
        return ValidationMode.AUTO;
    }

    @Override
    public Properties getProperties() {
        return new Properties();
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        return "1";
    }

    @Override
    public ClassLoader getClassLoader() {
        return WorldMod.class.getClassLoader();
    }

    @Override
    public void addTransformer(ClassTransformer transformer) {
        // wtf?
    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        return WorldMod.class.getClassLoader();
    }
}
