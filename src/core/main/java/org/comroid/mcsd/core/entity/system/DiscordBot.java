package org.comroid.mcsd.core.entity.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.comroid.annotations.Ignore;
import org.comroid.mcsd.core.entity.AbstractEntity;

@Getter
@Setter
@Entity
public class DiscordBot extends AbstractEntity {
    @Basic @Ignore @ToString.Exclude @Getter(onMethod = @__(@JsonIgnore))
    private String token;
    private int shardCount = 1;
}
