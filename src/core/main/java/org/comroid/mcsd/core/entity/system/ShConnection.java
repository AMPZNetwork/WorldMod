package org.comroid.mcsd.core.entity.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import org.comroid.api.func.util.Bitmask;
import org.comroid.mcsd.core.entity.AbstractEntity;

@Getter
@Setter
@Entity
public class ShConnection extends AbstractEntity {
    private @Basic String host;
    private int port = 22;
    private String username;
    private @Getter(onMethod = @__(@JsonIgnore)) String password;
    private long capabilites = Bitmask.combine(Capability.SSH);
    private @Deprecated String backupsDir = "$HOME/backups";

    @Override
    public String toString() {
        return "%s@%s:%d".formatted(username, host, port);
    }

    public enum Capability implements Bitmask.Attribute<Capability> { SSH, SFTP, FTP }
}
