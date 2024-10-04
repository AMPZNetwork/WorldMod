package com.ampznetwork.worldmod.api.model.region;

import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.libmod.api.model.EntityType;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.mini.Prioritized;
import com.ampznetwork.worldmod.api.model.mini.PropagationController;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import org.comroid.api.Polyfill;
import org.comroid.api.attr.Named;
import org.jetbrains.annotations.Nullable;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Value
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
//@RequiredArgsConstructor
@Table(name = "worldmod_region_groups")
public class Group extends DbObject.WithName implements PropagationController, Prioritized, Named {
    public static final                                                                     EntityType<Group, Builder<Group, ?>> TYPE     = Polyfill.uncheckedCast(
            new EntityType<>(
                    Group::builder,
                    null,
                    Group.class,
                    Group.Builder.class));
    @Default                                                                                long                                 priority = 0;
    @Singular("owner") @ManyToMany @CollectionTable(name = "worldmod_region_group_owners")  Set<Player>                          owners   = new HashSet<>();
    @Singular("member") @ManyToMany @CollectionTable(name = "worldmod_region_group_members") Set<Player>                          members  = new HashSet<>();
    @Column(name = "flag") @CollectionTable(name = "worldmod_region_group_flags", joinColumns = @JoinColumn(name = "id"))
    @ElementCollection(fetch = FetchType.EAGER) @Singular("flag") @Convert(converter = Flag.Usage.Converter.class)
    Set<Flag.Usage> declaredFlags = new HashSet<>();

    @Override
    public @Nullable Player getClaimOwner() {
        return null;
    }

    @Override
    public Stream<Flag.Usage> streamDeclaredFlags() {
        return declaredFlags.stream();
    }
}
