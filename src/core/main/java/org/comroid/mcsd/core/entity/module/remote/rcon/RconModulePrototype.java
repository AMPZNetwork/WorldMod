package org.comroid.mcsd.core.entity.module.remote.rcon;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.comroid.api.net.Token;
import org.comroid.mcsd.core.MCSD;
import org.comroid.mcsd.core.entity.module.console.ConsoleModulePrototype;
import org.comroid.mcsd.core.model.ServerPropertiesModifier;
import org.comroid.mcsd.core.util.ApplicationContextProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Properties;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RconModulePrototype extends ConsoleModulePrototype implements ServerPropertiesModifier {
    public static final int DefaultPort = 25575;

    private @Nullable Integer port;
    private @Nullable @Getter(onMethod = @__(@JsonIgnore)) String password;

    public String regeneratePassword() {
        var repo = ApplicationContextProvider.bean(MCSD.class).getModules_rcon();
        password = Token.random(16, false);
        repo.save(this);
        return password;
    }

    @Override
    public void modifyServerProperties(Properties prop) {
        var enable = password != null && !password.isBlank();
        prop.setProperty("enable-rcon", String.valueOf(enable));
        if (!enable)
            return;
        prop.setProperty("rcon.port", Optional.ofNullable(port).or(()->Optional.of(DefaultPort)).map(String::valueOf).orElseThrow());
        prop.setProperty("rcon.password", password);
    }
}
