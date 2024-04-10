package org.comroid.mcsd.core.entity.module.console;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.comroid.mcsd.core.entity.module.ModulePrototype;

@Log
@Getter
@Setter
@Entity
public abstract class ConsoleModulePrototype extends ModulePrototype {
}
