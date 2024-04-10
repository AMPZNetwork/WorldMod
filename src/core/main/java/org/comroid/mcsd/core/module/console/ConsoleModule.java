package org.comroid.mcsd.core.module.console;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;
import org.comroid.api.func.util.Bitmask;
import org.comroid.api.func.util.Event;
import org.comroid.mcsd.core.entity.module.console.ConsoleModulePrototype;
import org.comroid.mcsd.core.entity.server.Server;
import org.comroid.mcsd.core.module.ServerModule;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Log
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class ConsoleModule<T extends ConsoleModulePrototype> extends ServerModule<T> {
    public static final Pattern McsdPattern = commandPattern("mcsd");
    private Direction direction;

    public ConsoleModule(Direction direction, Server server, T proto) {
        super(server, proto);
        this.direction = direction;
    }

    public static Pattern commandPattern(String command) {return pattern("(?<username>[\\S\\w_-]+) issued parent command: /"+command+" (?<command>[\\w\\s_-]+)\\r?\\n?.*");}

    protected Event.Bus<String> bus;

    @Override
    protected void $initialize() {
        bus = new Event.Bus<>();
    }

    @Override
    protected void $terminate() {
        bus.close();
        super.$terminate();
    }

    public static Pattern pattern(@NotNull @Language("RegExp") String pattern) {
        return Pattern.compile(".*INFO]( \\[\\w*/\\w*])?: "+pattern);
    }

    public CompletableFuture<String> execute(String input) {
        return execute(input, null);
    }

    public abstract CompletableFuture<String> execute(String input, @Nullable Pattern terminator);

    @Getter
    @AllArgsConstructor
    public enum Direction implements Bitmask.Attribute<Direction> {
        Output(1L),
        Input(2L),
        Bidirectional(3L);

        private final @NotNull Long value;
    }
}
