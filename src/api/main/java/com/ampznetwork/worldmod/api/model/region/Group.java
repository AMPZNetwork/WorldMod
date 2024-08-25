package com.ampznetwork.worldmod.api.model.region;

import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.libmod.api.model.EntityType;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.mini.Prioritized;
import com.ampznetwork.worldmod.api.model.mini.PropagationController;
import lombok.AllArgsConstructor;
import lombok.Builder.Default;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import org.comroid.api.attr.Named;

import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Value
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Group extends DbObject.WithName implements PropagationController, Prioritized, Named {
    public static final EntityType<Group, Builder> TYPE
                                                                          = new EntityType<>(
            Group::builder,
            null,
            Group.class,
            Group.Builder.class);
    @Default            long                       priority               = 0;
    @ElementCollection(fetch = FetchType.EAGER) @Singular("owner")
    Set<UUID>       ownerIDs      = new HashSet<>();
    @ElementCollection(fetch = FetchType.EAGER) @Singular("member")
    Set<UUID>       memberIDs     = new HashSet<>();
    @ElementCollection(fetch = FetchType.EAGER) @Singular("flag") @Convert(converter = Flag.Usage.Converter.class)
    Set<Flag.Usage> declaredFlags = new HashSet<>();

    @Override
    public Stream<Flag.Usage> streamDeclaredFlags() {
        return declaredFlags.stream();
    }
}
