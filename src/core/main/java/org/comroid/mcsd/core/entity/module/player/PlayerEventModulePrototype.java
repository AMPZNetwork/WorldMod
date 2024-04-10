package org.comroid.mcsd.core.entity.module.player;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.comroid.mcsd.core.entity.module.ModulePrototype;
import org.comroid.mcsd.core.module.console.ConsoleModule;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class PlayerEventModulePrototype extends ModulePrototype {
    public static final Pattern DefaultChatPattern = ConsoleModule.pattern(
            "([(\\[{<](?<prefix>[\\w\\s_-]+)[>}\\])]\\s?)*" +
                    //"([(\\[{<]" +
                    "<" +
                    "(?<username>[\\w\\S_-]+)" +
                    ">\\s?" +
                    //"[>}\\])]\\s?)\\s?" +
                    "([(\\[{<](?<suffix>[\\w\\s_-]+)[>}\\])]\\s?)*" +
                    "(?<message>.+)\\r?\\n?.*");
    public static final Pattern DefaultBroadcastPattern = ConsoleModule.pattern(
            "(?<username>[\\S\\w_-]+) issued parent command: " +
                    "/(?<command>(me)|(say)|(broadcast)) " +
                    "(?<message>.+)\\r?\\n?.*");
    public static final Pattern DefaultJoinLeavePattern = ConsoleModule.pattern(
            "(?<username>[\\S\\w_-]+) " +
                    "(?<message>(joined|left) the game)\\r?\\n?");
    public static final Pattern DefaultAchievementPattern = ConsoleModule.pattern(
            "(?<username>[\\S\\w_-]+) " +
                    "(?<message>has (made the advancement|completed the challenge) " +
                    "(\\[(?<advancement>[\\w\\s]+)]))\\r?\\n?");

    private @Nullable @Basic String chatPattern;
    private @Nullable String broadcastPattern;
    private @Nullable String joinLeavePattern;
    private @Nullable String achievementPattern;
}
