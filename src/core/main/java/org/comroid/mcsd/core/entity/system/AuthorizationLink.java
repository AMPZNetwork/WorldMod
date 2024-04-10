package org.comroid.mcsd.core.entity.system;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.comroid.annotations.Readonly;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
public class AuthorizationLink {
    private @Id String code;
    private @Readonly @ManyToOne User creator;
    private UUID target;
    private long permissions;
    private Instant validUntil;

    public AuthorizationLink(String code, User creator, UUID target, long permissions) {
        this.code = code;
        this.creator = creator;
        this.target = target;
        this.permissions = permissions;
        this.validUntil = Instant.now().plus(Duration.ofHours(6));
    }

    public String getUrlPath() {
        return "/api/authorize?code=" + code;
    }
}
