package com.ampznetwork.worldmod.api.model.region;

import com.ampznetwork.libmod.api.entity.DbObject;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.mini.Prioritized;
import com.ampznetwork.worldmod.api.model.mini.PropagationController;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Singular;
import lombok.Value;
import org.comroid.api.attr.Named;

import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Value
@Entity
@Builder(toBuilder = true)
public class Group extends DbObject implements PropagationController, Prioritized, Named {
    @Id String name;
    @Default long priority = 0;
    @ElementCollection(fetch = FetchType.EAGER) @Singular("owner") Set<UUID> ownerIDs;
    @ElementCollection(fetch = FetchType.EAGER) @Singular("member") Set<UUID> memberIDs;
    @ElementCollection(fetch = FetchType.EAGER) @Singular("flag") @Convert(converter = Flag.Usage.Converter.class) Set<Flag.Usage> declaredFlags;

    @Override
    public Stream<Flag.Usage> streamDeclaredFlags() {
        return declaredFlags.stream();
    }
}
