package com.ampznetwork.worldmod.core.query;

import com.ampznetwork.libmod.api.entity.Player;
import com.ampznetwork.worldmod.api.WorldMod;
import com.ampznetwork.worldmod.api.game.Flag;
import com.ampznetwork.worldmod.api.model.query.IWorldQuery;
import com.ampznetwork.worldmod.api.model.query.QueryInputData;
import com.ampznetwork.worldmod.api.model.query.QueryVerb;
import com.ampznetwork.worldmod.api.model.region.Group;
import com.ampznetwork.worldmod.api.model.region.Region;
import com.ampznetwork.worldmod.core.query.condition.BlockTypeCondition;
import com.ampznetwork.worldmod.core.query.condition.FlagCondition;
import com.ampznetwork.worldmod.core.query.condition.PositionCondition;
import com.ampznetwork.worldmod.core.query.condition.QueryCondition;
import com.ampznetwork.worldmod.core.query.condition.RadiusCondition;
import com.ampznetwork.worldmod.core.query.condition.RegionNameCondition;
import com.ampznetwork.worldmod.core.query.condition.SourceCondition;
import com.ampznetwork.worldmod.core.query.condition.TagCondition;
import com.ampznetwork.worldmod.core.query.condition.TargetCondition;
import com.ampznetwork.worldmod.core.query.condition.TimeCondition;
import com.ampznetwork.worldmod.core.query.condition.WorldCondition;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.RequiredArgsConstructor;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.comroid.api.Polyfill;
import org.comroid.api.attr.Named;
import org.comroid.api.data.Vector;
import org.comroid.api.func.exc.ThrowingSupplier;
import org.comroid.api.func.util.Command;
import org.comroid.api.func.util.Debug;
import org.comroid.api.func.util.Streams;
import org.comroid.api.info.Log;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.Query;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Value
@Slf4j
@Builder
public class WorldQuery implements IWorldQuery {
    private static final String kvPairPattern = "[<>]?=";

    public static WorldQuery parseLookup(String query) {
        return parse(query.startsWith("lookup ") ? query : "lookup " + query);
    }

    public static WorldQuery parse(String query) {
        try {
            var builder = builder();
            var split   = query.split(" ");

            builder.verb(QueryVerb.valueOf(split[0].toUpperCase()));

            for (int i = 1; i < split.length; i++) {
                var str   = split[i];
                var pair = str.split(kvPairPattern);
                var key   = pair[0];
                var value = pair[1];
                var add = switch (key) {
                    case "region", "group" -> new RegionNameCondition(value, "group".equals(key), comparator(str, key, value));
                    case "source" -> new SourceCondition(value, comparator(str, key, value));
                    case "target" -> new TargetCondition(value, comparator(str, key, value));
                    case "radius" -> new RadiusCondition(wrapParseArg("radius", () -> Integer.parseInt(value)));
                    case "world" -> new WorldCondition(value, comparator(str, key, value));
                    case "since" -> new TimeCondition(wrapParseArg("duration", () -> Instant.now().minus(Polyfill.parseDuration(value))));
                    case "type" -> new BlockTypeCondition(value, comparator(str, key, value));
                    case "flag" -> new FlagCondition(wrapParseArg("flag", () -> Flag.getForName(value)));
                    case "tag" -> new TagCondition(value, comparator(str, key, value));
                    case "x", "y", "z" -> wrapParseArg("coordinate", () -> {
                        // find bounds from available condition
                        Vector.N3 a, b = null;
                        var condition = builder.conditions == null
                                        ? null
                                        : builder.conditions.stream().flatMap(Streams.cast(PositionCondition.class)).findAny().orElse(null);
                        if (value.contains("..")) b = Optional.ofNullable(condition).map(PositionCondition::getB).orElseGet(Vector.N3::new);
                        if (condition == null) {
                            a         = new Vector.N3();
                            condition = new PositionCondition(new Comparator[3], a, b);
                        } else {
                            a = condition.getA();
                        }

                        // parse and set numbers
                        var    range = value.split("\\.\\.");
                        double aN    = Double.parseDouble(range[0]);
                        Double bN    = range.length > 1 ? Double.parseDouble(range[1]) : null;
                        int    dim   = key.toLowerCase().charAt(0) - 'x';
                        a.set(dim, aN);
                        if (b != null && bN != null) b.set(dim, bN);
                        else condition.getComparators()[dim] = comparator(str, key, value);
                        return condition;
                    });
                    case "message" -> {
                        builder.messageKey(value);
                        yield null;
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + key);
                };
                if (add != null && (builder.conditions == null || !builder.conditions.contains(add))) builder.condition(add);
            }

            return builder.build();
        } catch (Throwable t) {
            throw wrapExc("Unable to parse query '" + query + "'", t);
        }
    }

    @NotNull           QueryVerb            verb;
    @Nullable @Default String               messageKey = null;
    @Singular          List<QueryCondition> conditions;

    @Override
    public Optional<TextComponent> getMessage(WorldMod mod) {
        return Optional.ofNullable(messageKey).map(mod.getMessages()::getProperty).map(LegacyComponentSerializer.legacyAmpersand()::deserialize);
    }

    @Override
    public Query toLookupQuery(WorldMod mod) {
        @Language("SQL") String query = """
                select e.* from world_log e where e.id = p.id
                """;
        var append = new ArrayList<String>();
        var params = new HashMap<String, Object>();
        conditions.stream().flatMap(Streams.cast(PositionCondition.class)).findAny().ifPresent(pos -> {/*todo*/});
        conditions.stream().flatMap(Streams.cast(FlagCondition.class)).findAny().ifPresent(flag -> {
            append.add("and e.action = :flag");
            params.put("flag", flag.flag().getName());
        });
        conditions.stream().flatMap(Streams.cast(TimeCondition.class)).findAny().ifPresent(time -> {
            append.add("and e.timestamp > :since");
            params.put("since", time.since());
        });
        conditions.stream().flatMap(Streams.cast(SourceCondition.class)).findAny().ifPresent(source -> {
            append.add("and (e.player.id = :playerId or e.nonPlayerSource = :target)");
            params.put("playerId", source.source()); // todo this needs name->uuid conversion
            params.put("target", source.source());
        });
        /* todo: target condition
        conditions.stream()
                .flatMap(Streams.cast(SourceCondition.class))
                .findAny()
                .ifPresent(target -> {
                    append.add("and (e.player.id = :playerId or e.nonPlayerSource = :target)");
                    params.put("playerId", EventDispatchBase.tryGetAsPlayer(mod, target.target()));
                    params.put("target", target.target());
                });
         */

        return mod.getEntityService().createQuery(mgr -> {
            //noinspection SqlSourceToSinkFlow
            var q = mgr.createQuery(query + String.join("\n", append) + ';');
            params.forEach(q::setParameter);
            return q;
        });
    }

    @Override
    public String toString() {
        return verb.name().toLowerCase() + ' ' + conditions.stream().map(Object::toString).collect(Collectors.joining(" ")) + (messageKey == null
                                                                                                                               ? ""
                                                                                                                               : " message=" + messageKey);
    }

    @Override
    public boolean test(WorldMod mod, QueryInputData data) {
        try {
            return conditions.stream().allMatch(cond -> cond.test(mod, this, data, null));
        } catch (Throwable t) {
            Log.at(Level.WARNING, "Could not evaluate " + this, t);
            return false;
        }
    }

    public enum ConditionType implements ValueAutofillOptionsProvider {
        REGION("#global") {
            @Override
            public Stream<String> autoFillValue(Command.Usage usage, String argName, WorldMod mod, String value) {
                return mod.getEntityService().getAccessor(Region.TYPE).all().map(Named::getName);
            }
        }, GROUP {
            @Override
            public Stream<String> autoFillValue(Command.Usage usage, String argName, WorldMod mod, String value) {
                return mod.getEntityService().getAccessor(Group.TYPE).all().map(Named::getName);
            }
        }, SOURCE("@a") {
            @Override
            public Stream<String> autoFillValue(Command.Usage usage, String argName, WorldMod mod, String value) {
                return Stream.concat(
                        // player names, todo: selectors
                        mod.getPlayerAdapter().getCurrentPlayers().map(Player::getName),
                        // entity types
                        mod.getLib().entityTypes());
            }
        }, TARGET("@a") {
            @Override
            public Stream<String> autoFillValue(Command.Usage usage, String argName, WorldMod mod, String value) {
                return Stream.concat(
                        // player names, todo: selectors
                        mod.getPlayerAdapter().getCurrentPlayers().map(Player::getName),
                        // material keys
                        mod.getLib().materials());
            }
        }, RADIUS(NUMERICS), WORLD {
            @Override
            public Stream<String> autoFillValue(Command.Usage usage, String argName, WorldMod mod, String value) {
                return mod.getLib().worldNames();
            }
        }, SINCE {
            @Override
            public Stream<String> autoFillValue(Command.Usage usage, String argName, WorldMod mod, String value) {
                return Command.AutoFillProvider.Duration.INSTANCE.autoFill(usage, argName, value);
            }
        }, TYPE, FLAG {
            @Override
            public Stream<String> autoFillValue(Command.Usage usage, String argName, WorldMod mod, String value) {
                return mod.flagNames();
            }
        }, TAG, X(NUMERICS), Y(NUMERICS), Z(NUMERICS), MESSAGE {
            @Override
            public Stream<String> autoFillValue(Command.Usage usage, String argName, WorldMod mod, String value) {
                return mod.getMessages().keySet().stream().map(String::valueOf);
            }
        };
        private final @Nullable ValueAutofillOptionsProvider delegate;
        private final           String[]                     constants;

        ConditionType(String... constants) {
            this(null, constants);
        }

        ConditionType(@Nullable ValueAutofillOptionsProvider delegate, String... constants) {
            this.delegate  = delegate;
            this.constants = constants;
        }

        @Override
        public Stream<String> autoFillValue(Command.Usage usage, String argName, WorldMod mod, String value) {
            return Stream.concat(Arrays.stream(constants), delegate == null ? Stream.of(value) : delegate.autoFillValue(usage, argName, mod, value));
        }
    }

    @RequiredArgsConstructor
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    public enum Comparator implements BiPredicate<Object, Object> {
        EQUALS("=") {
            @Override
            public boolean test(Object l, Object r) {
                return Objects.equals(l, r);
            }
        }, SIMILAR("~") {
            @Override
            public boolean test(Object l, Object r) {
                if (l instanceof String str) return str.contains(String.valueOf(r));
                if (l instanceof Integer x && r instanceof Integer y) return Math.max(x, y) - Math.min(x, y) < 16;
                if (l instanceof Double x && r instanceof Double y) return Math.max(x, y) - Math.min(x, y) < 16;
                return EQUALS.test(l, r);
            }
        }, GREATER(">") {
            @Override
            public boolean test(Object l, Object r) {
                if (l instanceof Integer x && r instanceof Integer y) return x > y;
                if (l instanceof Double x && r instanceof Double y) return x > y;
                return EQUALS.test(l, r);
            }
        }, GREATER_EQUALS(">=") {
            @Override
            public boolean test(Object l, Object r) {
                if (l instanceof Integer x && r instanceof Integer y) return x >= y;
                if (l instanceof Double x && r instanceof Double y) return x >= y;
                return EQUALS.test(l, r);
            }
        }, LESSER("<") {
            @Override
            public boolean test(Object l, Object r) {
                if (l instanceof Integer x && r instanceof Integer y) return x < y;
                if (l instanceof Double x && r instanceof Double y) return x < y;
                return EQUALS.test(l, r);
            }
        }, LESSER_EQUALS("<=") {
            @Override
            public boolean test(Object l, Object r) {
                if (l instanceof Integer x && r instanceof Integer y) return x <= y;
                if (l instanceof Double x && r instanceof Double y) return x <= y;
                return EQUALS.test(l, r);
            }
        };

        public static Comparator find(String string) {
            return Arrays.stream(values()).filter(comp -> comp.string.equals(string)).findAny().orElseThrow();
        }

        String string;

        @Override
        public abstract boolean test(Object base, Object key);
    }

    public interface ValueAutofillOptionsProvider {
        ValueAutofillOptionsProvider NUMERICS = ($0, $1, mod, value) -> Stream.of(value)
                .flatMap(num -> Stream.concat(Stream.of(num), IntStream.range(0, 10).mapToObj(digit -> num + digit)))
                .flatMap(num -> Stream.concat(Stream.of(num),
                        Stream.of(num)
                                .map(str -> str + "..")
                                .flatMap(str -> Stream.concat(Stream.of(str), IntStream.range(0, 10).mapToObj(digit -> str + digit)))))
                .map(str -> str.replaceAll("\\.{3,}", ".."))
                .distinct()
                .sorted(java.util.Comparator.comparingInt(String::length));

        Stream<String> autoFillValue(Command.Usage usage, String argName, WorldMod mod, String value);
    }

    @Value
    public class AutoFillProvider implements Command.AutoFillProvider {
        @Override
        public Stream<String> autoFill(Command.Usage usage, String argName, String currentValue) {
            var split = currentValue.split(" ");
            if (split.length <= 1)
                // return possible verbs
                return Arrays.stream(QueryVerb.values()).map(java.lang.Enum::name).map(String::toLowerCase);
            split = split[split.length - 1].split(kvPairPattern);
            return split.length <= 1 ?
                   // return possible condition keys
                   Arrays.stream(ConditionType.values()).map(java.lang.Enum::name).map(String::toLowerCase) :
                   // else return value-dependent autofill
                   ConditionType.valueOf(split[0].toUpperCase())
                           .autoFillValue(usage, argName, usage.getContext().stream().flatMap(Streams.cast(WorldMod.class)).findAny().orElseThrow(), split[1]);
        }
    }

    private static Comparator comparator(String str, String key, String value) {
        return wrapParseArg("comparator", () -> Comparator.find(str.substring(key.length(), str.indexOf(value, key.length()))));
    }

    private static <T> T wrapParseArg(String nameof, ThrowingSupplier<T, Throwable> parse) {
        try {
            return parse.get();
        } catch (Throwable t) {
            throw wrapExc("Cannot parse argument " + nameof, t);
        }
    }

    private static Command.Error wrapExc(String detail, Throwable t) {
        Debug.log(log, detail, t);
        return new Command.Error(detail + "; " + t.toString());
    }
}
