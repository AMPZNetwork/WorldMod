package com.ampznetwork.worldmod.core.database.hibernate;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.database.EntityService;
import com.ampznetwork.worldmod.api.model.mini.RegionCompositeKey;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.comroid.api.data.Vector;
import org.comroid.api.info.Constraint;
import org.comroid.api.tree.Container;
import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Stream;

import static org.comroid.api.func.util.Debug.isDebug;

@Slf4j
public class HibernateEntityService extends Container.Base implements EntityService {
    private final WorldMod worldMod;
    private final EntityManager manager;

    public HibernateEntityService(WorldMod worldMod, DatabaseType type, String url, String user, String pass) {
        this.worldMod = worldMod;

        var config = Map.of(
                "hibernate.connection.driver_class", type.getDriverClass().getCanonicalName(),
                "hibernate.connection.url", url,
                "hibernate.connection.username", user,
                "hibernate.connection.password", pass,
                "hibernate.dialect", type.getDialect(),
                "hibernate.hbm2ddl.auto", "update",
                "hibernate.show_sql", String.valueOf(isDebug())
        );
        var provider = new HibernatePersistenceProvider();
        var dataSource = new HikariDataSource() {{
            setDriverClassName(type.getDriverClass().getCanonicalName());
            setJdbcUrl(url);
            setUsername(user);
            setPassword(pass);
        }};
        var unit = new WorldModPersistenceUnit(dataSource);
        var factory = provider.createContainerEntityManagerFactory(unit, config);
        this.manager = factory.createEntityManager();

        addChildren(dataSource, factory, manager);
    }

    @Override
    public Optional<Region> findRegion(RegionCompositeKey key) {
        return Optional.ofNullable(manager.find(Region.class, key));
    }

    @Override
    public Optional<Region> findRegion(Vector.N3 location, String worldName) {
        // todo: query, needs area in db
        return manager.createQuery("SELECT r FROM Region r", Region.class)
                .getResultStream()
                .sorted(Comparator.comparingLong(Region::getPriority))
                .filter(region -> region.isPointInside(location))
                .findFirst();
    }

    @Override
    public Stream<Region> findClaims(UUID claimOwnerId) {
        return manager.createQuery("SELECT Region FROM Region r WHERE r.claimOwner = :id", Region.class)
                .setParameter("id", claimOwnerId)
                .getResultStream();
    }

    @Override
    public Optional<Group> findGroup(String name) {
        return Optional.ofNullable(manager.find(Group.class, name));
    }

    @Override
    public boolean save(Object... entities) {
        var transaction = manager.getTransaction();
        synchronized (transaction) {
            try {
                transaction.begin();
                for (Object each : entities)
                    manager.persist(each);
                manager.flush();
                transaction.commit();
            } catch (Throwable t) {
                transaction.rollback();
                log.warn("Could not save all entities\n\tEntities: "+ Arrays.toString(entities), t);
                return false;
            }
        }
        return true;
    }

    @Override
    public <T> T refresh(T it) {
        Constraint.notNull(it, "entity");
        manager.refresh(it);
        return it;
    }
}
