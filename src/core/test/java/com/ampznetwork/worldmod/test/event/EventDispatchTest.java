package com.ampznetwork.worldmod.test.event;

import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.database.EntityService;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.math.Shape;
import com.ampznetwork.worldmod.api.model.adp.IPropagationAdapter;
import com.ampznetwork.worldmod.api.model.adp.PlayerAdapter;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.api.model.sel.Area;
import com.ampznetwork.worldmod.core.event.EventDispatchBase;
import lombok.Data;
import net.kyori.adventure.util.TriState;
import org.comroid.api.data.Vector;
import org.comroid.api.text.minecraft.Tellraw;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static com.ampznetwork.worldmod.api.game.Flag.*;

public class EventDispatchTest {
    final UUID   PlayerOwner  = UUID.randomUUID();
    final UUID   PlayerMember = UUID.randomUUID();
    final UUID   PlayerGuest  = UUID.randomUUID();
    final Vector.N3 LocationInside;
    final Vector.N3 LocationOutside;
    final Region Region;
    WorldMod mod;
    EventDispatchBase dispatch;

    public EventDispatchTest() {
        LocationInside = new Vector.N3(8, 0, 8);
        LocationOutside = new Vector.N3(-1, 0, -1);
        Region         = com.ampznetwork.worldmod.api.model.region.Region.builder()
                .name("testregion")
                .area(new Area(Shape.Cuboid, List.of(
                        new Vector.N4(0, 0, 0, 0),
                        new Vector.N4(16, 0, 16, 0)
                )))
                .owner(PlayerOwner)
                .member(PlayerMember)
                .declaredFlags(List.of(
                        Flag.Usage.builder()
                                .flag(Build)
                                .state(TriState.TRUE)
                                .force(true)
                                .selectors(Set.of(Tellraw.Selector.builder()
                                        .base(Tellraw.Selector.Base.NEAREST_PLAYER)
                                        .type("owner")
                                        .build()))
                                .build(),
                        Flag.Usage.builder()
                                .flag(Build)
                                .state(TriState.TRUE)
                                .selectors(Set.of(Tellraw.Selector.builder()
                                        .base(Tellraw.Selector.Base.NEAREST_PLAYER)
                                        .type("member")
                                        .build()))
                                .build()
                ))
                .build();
    }

    @BeforeEach
    public void setup() {
        this.mod = new WorldMod() {
            @Override
            public EntityService getEntityService() {
                return new EntityService() {
                    @Override
                    public Optional<com.ampznetwork.worldmod.api.model.region.Region> findRegion(RegionCompositeKey key) {
                        return key.equals(Region.key()) ? Optional.of(Region) : Optional.empty();
                    }

                    @Override
                    public Stream<com.ampznetwork.worldmod.api.model.region.Region> findRegions(Vector.N3 location, String worldName) {
                        return Stream.of(Region)
                                .filter(rg -> rg.getWorldName().equals(worldName) && rg.isPointInside(location));
                    }

                    @Override
                    public Stream<com.ampznetwork.worldmod.api.model.region.Region> findClaims(UUID claimOwnerId) {
                        return Stream.of(Region)
                                .filter(rg -> claimOwnerId.equals(rg.getClaimOwner()));
                    }

                    @Override
                    public Optional<Group> findGroup(String name) {
                        return Optional.empty();
                    }

                    @Override
                    public boolean save(Object... it) {
                        return false;
                    }

                    @Override
                    public <T> T refresh(T it) {
                        return it;
                    }
                };
            }

            @Override
            public PlayerAdapter getPlayerAdapter() {
                return null;
            }
        };
        this.dispatch = new EventDispatchBase(mod);
    }

    private void testPropagate(UUID player, Vector.N3 location, int expect) {
        var propAdp = new PropagationAdapter();
        dispatch.dispatchEvent(propAdp, player, location, "world", Build);
        Assertions.assertEquals(expect, propAdp.state(), "Invalid Event cancellation state");
    }

    @Test
    public void testPropagate_Owner() {
        testPropagate(PlayerOwner, LocationInside, 2);
        testPropagate(PlayerOwner, LocationOutside, 0);
    }

    @Test
    public void testPropagate_Member() {
        testPropagate(PlayerMember, LocationInside, 0);
        testPropagate(PlayerMember, LocationOutside, 0);
    }

    @Test
    public void testPropagate_Guest() {
        testPropagate(PlayerGuest, LocationInside, 1);
        testPropagate(PlayerGuest, LocationOutside, 0);
    }

    @Data
    @SuppressWarnings("ConstantValue")
    public static class PropagationAdapter implements IPropagationAdapter {
        private boolean cancel = false;
        private boolean force = false;

        @Override
        public void cancel() {
            cancel = true;
        }

        @Override
        public void force() {
            force = true;
        }

        public int state() {
            return (cancel ? 1 : 0) | (force ? 2 : 0);
        }
    }
}
