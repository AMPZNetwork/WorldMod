package org.comroid.mcsd.core.module.player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;
import org.comroid.api.func.util.Event;
import org.comroid.api.func.util.Streams;
import org.comroid.api.java.Switch;
import org.comroid.mcsd.api.dto.comm.PlayerEvent;
import org.comroid.mcsd.core.entity.module.player.ConsolePlayerEventModulePrototype;
import org.comroid.mcsd.core.entity.server.Server;
import org.comroid.mcsd.core.module.console.ConsoleModule;
import org.springframework.util.StringUtils;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.stream.Stream;

@Log
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConsolePlayerEventModule extends PlayerEventModule<ConsolePlayerEventModulePrototype> {
    private @Inject ConsoleModule<?> console;

    public ConsolePlayerEventModule(Server server, ConsolePlayerEventModulePrototype proto) {
        super(server, proto);
    }

    @Override
    @SuppressWarnings({"RedundantSuppression", "RedundantTypeArguments", "RedundantCast"}) // intellij is being weird
    protected Event.Bus<PlayerEvent> initEventBus() {
        //noinspection unchecked
        return console.getBus()
                .<Matcher>mapData(str -> Stream.of(ChatPattern, BroadcastPattern, JoinLeavePattern, AchievementPattern)
                        .collect(Streams.append(DeathMessagePatterns))
                        .<Matcher>flatMap(pattern -> {
                            var matcher = pattern.matcher(String.valueOf(str));
                            if (matcher.matches())
                                return Stream.of(matcher);
                            return Stream.<Matcher>empty();
                        })
                        .findAny()
                        .<Matcher>orElse((Matcher) null))
                .mapData(matcher -> {
                    var username = matcher.group("username");
                    var message = matcher.group("message");
                    //noinspection SuspiciousMethodCalls
                    var type = new Switch<>(() -> PlayerEvent.Type.Other)
                            .option(ChatPattern, PlayerEvent.Type.Chat)
                            .option(JoinLeavePattern, PlayerEvent.Type.JoinLeave)
                            .option(AchievementPattern, PlayerEvent.Type.Achievement)
                            .option(DeathMessagePatterns::contains, PlayerEvent.Type.Death)
                            .apply(matcher.pattern());
                    if (type != PlayerEvent.Type.Chat)
                        message = StringUtils.capitalize(message);
                    return new PlayerEvent(username, message, type);
                })
                .peekData(msg -> log.log(Level.FINE, "[CHAT @ %s] <%s> %s".formatted(server, msg.getUsername(), msg)));
    }
}
