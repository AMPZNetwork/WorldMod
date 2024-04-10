package org.comroid.mcsd.core.module.player;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;
import org.comroid.api.func.util.Event;
import org.comroid.api.func.util.Streams;
import org.comroid.mcsd.api.dto.comm.PlayerEvent;
import org.comroid.mcsd.core.entity.module.player.PlayerEventModulePrototype;
import org.comroid.mcsd.core.entity.server.Server;
import org.comroid.mcsd.core.module.ServerModule;
import org.comroid.mcsd.core.module.console.ConsoleModule;
import org.comroid.api.text.minecraft.McFormatCode;
import org.comroid.api.text.minecraft.Tellraw;
import org.intellij.lang.annotations.Language;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.time.Instant.now;
import static org.comroid.mcsd.core.util.ApplicationContextProvider.bean;

@Log
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class PlayerEventModule<T extends PlayerEventModulePrototype> extends ServerModule<T> {
    public static final Duration TickerTimeout = Duration.ofMinutes(15);
    public static final Pattern ChatPattern = ConsoleModule.pattern(
            "([(\\[{<](?<prefix>[\\w\\s_-]+)[>}\\])]\\s?)*" +
                    //"([(\\[{<]" +
                    "<" +
                    "(?<username>[\\w\\S_-]+)" +
                    ">\\s?" +
                    //"[>}\\])]\\s?)\\s?" +
                    "([(\\[{<](?<suffix>[\\w\\s_-]+)[>}\\])]\\s?)*" +
                    "(?<message>.+)\\r?\\n?.*");
    public static final Pattern BroadcastPattern = ConsoleModule.pattern(
            "(?<username>[\\S\\w_-]+) issued parent command: " +
                    "/(?<command>(me)|(say)|(broadcast)) " +
                    "(?<message>.+)\\r?\\n?.*");
    public static final Pattern JoinLeavePattern = ConsoleModule.pattern(
            "(?<username>[\\S\\w_-]+) " +
                    "(?<message>(joined|left) the game)\\r?\\n?");
    public static final Pattern AchievementPattern = ConsoleModule.pattern(
            "(?<username>[\\S\\w_-]+) " +
                    "(?<message>has (made the advancement|completed the challenge) " +
                    "(\\[(?<advancement>[\\w\\s]+)]))\\r?\\n?");
    public static final Pattern DeathPatternBase = Pattern.compile("^%1\\$s (?<message>[\\w\\s%$]+)$");
    public static final @Language("RegExp") String DeathPatternScheme = ".*INFO]: (?<username>[\\S\\w-_]+) (?<message>%s)\\r?\\n?.*";
    @SuppressWarnings("RegExpDuplicateCharacterInClass")
    public static final @Language("RegExp") String CleanWord_Spaced = "\\\\[?([\\\\s\\\\w\\\\-_'?!.]+)]?";
    public static final List<Pattern> DeathMessagePatterns = new ArrayList<>();
    private final AtomicReference<TickerMessage> lastTickerMessage = new AtomicReference<>(new TickerMessage(now(), -1));
    protected @Getter Event.Bus<PlayerEvent> bus;
    
    static {
        try (var url = new URL("https://raw.githubusercontent.com/misode/mcmeta/assets-json/assets/minecraft/lang/en_us.json").openStream()) {
            var obj = bean(ObjectMapper.class).readTree(url);
            Streams.of(obj.fields(), obj.size())
                    .filter(e -> e.getKey().startsWith("death."))
                    .map(Map.Entry::getValue)
                    .map(JsonNode::asText)
                    .filter(Objects::nonNull)
                    .flatMap(str -> {
                        var matcher = DeathPatternBase.matcher(str);
                        if (!matcher.matches())
                            return Stream.empty();
                        var msg = matcher.group("message");
                        var out = DeathPatternScheme.formatted(msg).replaceAll("%\\d\\$s", CleanWord_Spaced);
                        return Stream.of(out);
                    })
                    .map(Pattern::compile)
                    .forEach(DeathMessagePatterns::add);
            log.info(DeathMessagePatterns.size() + " death messages registered");
        } catch (Throwable t) {
            log.log(Level.INFO, "Unable to fetch death messages; disabling support for it", t);
        }
    }

    public PlayerEventModule(Server server, T proto) {
        super(server, proto);
    }

    protected abstract Event.Bus<PlayerEvent> initEventBus();

    @Override
    protected void $initialize() {
        bus = initEventBus();
    }

    @Override
    protected void $tick() {
        var console = server.component(ConsoleModule.class);
        var msgs = server.getTickerMessages();
        var last = lastTickerMessage.get();
        if (console.isNull() || msgs.isEmpty() || last.time.plus(TickerTimeout).isAfter(now()))
            return;
        var i = last.index + 1;
        if (i >= msgs.size())
            i = 0;
        var msg = msgs.get(i);
        var cmd = Tellraw.Command.builder()
                .selector(Tellraw.Selector.Base.ALL_PLAYERS)
                .component(McFormatCode.Gray.text("<").build())
                .component(McFormatCode.Light_Purple.text(msg).build())
                .component(McFormatCode.Gray.text("> ").build())
                .build().toString();
        console.assertion().execute(cmd);
    }

    private record TickerMessage(Instant time, int index) {
    }
}
