package org.comroid.mcsd.core.entity.module.remote.ssh;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.comroid.mcsd.core.entity.module.FileModulePrototype;
import org.comroid.mcsd.core.entity.system.ShConnection;
import org.jetbrains.annotations.NotNull;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SshFileModulePrototype extends FileModulePrototype {
    private @NotNull @ManyToOne ShConnection shConnection;
}
