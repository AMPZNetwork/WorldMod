package org.comroid.mcsd.core.module.discord;

import emoji4j.EmojiUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import org.comroid.api.Polyfill;
import org.comroid.api.func.exc.ThrowingSupplier;
import org.comroid.api.func.ext.Wrap;
import org.comroid.mcsd.api.model.IStatusMessage;
import org.comroid.mcsd.core.entity.module.discord.DiscordModulePrototype;
import org.comroid.mcsd.core.entity.server.Server;
import org.comroid.mcsd.core.entity.system.User;
import org.comroid.mcsd.core.model.DiscordMessageSource;
import org.comroid.mcsd.core.module.ServerModule;
import org.comroid.mcsd.core.module.console.ConsoleModule;
import org.comroid.mcsd.core.module.player.PlayerEventModule;
import org.comroid.mcsd.core.module.status.StatusModule;
import org.comroid.mcsd.core.repo.system.UserRepo;
import org.comroid.api.text.minecraft.Tellraw;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.comroid.mcsd.core.util.ApplicationContextProvider.bean;
import static org.comroid.api.text.minecraft.McFormatCode.*;
import static org.comroid.api.text.minecraft.Tellraw.Event.Action.open_url;
import static org.comroid.api.text.minecraft.Tellraw.Event.Action.show_text;

@Log
@Getter
@ToString
public class DiscordModule extends ServerModule<DiscordModulePrototype> {
    public static final String WebhookName = "MCSD Discord Hook";
    public static final Pattern EmojiPattern = Pattern.compile(".*:(?<name>[\\w-_]+):?.*");
    protected final DiscordAdapter adapter;
    private @Inject PlayerEventModule<?> events;
    private @Inject ConsoleModule<?> console;

    public DiscordModule(Server server, DiscordModulePrototype proto) {
        super(server, proto);
        this.adapter = Optional.ofNullable(proto.getDiscordBot())
                .map(DiscordAdapter::get)
                .orElseThrow();
    }

    @Override
    @SneakyThrows
    protected void $initialize() {
        var chat = events.getBus();

        adapter.getJda().awaitReady();

        // public channel
        Optional.ofNullable(proto.getPublicChannelId()).ifPresent(id -> {
            final var webhook = adapter.getWebhook(id)
                    .thenApply(adapter::messageTemplate).join();
            final var bot = adapter.messageTemplate(id);

            // public status -> dc
            server.component(StatusModule.class).map(StatusModule::getBus).ifPresent(bus ->
                    addChildren(bus.filterData(msg -> msg.getScope() == IStatusMessage.Scope.Public)
                            .mapData(msg -> new EmbedBuilder()
                                    .setAuthor(server.getAlternateName(),
                                            Optional.ofNullable(server.getHomepage())
                                                    .filter(Predicate.not(String::isBlank))
                                                    .orElse(server.getViewURL()))//,server.getThumbnailURL())
                                    .setDescription(msg.toStatusMessage())
                                    .setColor(msg.getStatus().getColor())
                                    .setFooter(msg.getMessage())
                                    .setTimestamp(Instant.now()))
                            .mapData(DiscordMessageSource::new)
                            .peekData(msg -> msg.setAppend(false))
                            .subscribeData(bot)));

            addChildren(
                    // mc -> dc
                    chat.filterData(msg -> msg.getType().isFlagSet(Objects.requireNonNullElse(proto.getPublicChannelEvents(), DiscordModulePrototype.DefaultPublicChannelEvents)))
                            .mapData(msg -> {
                                var player = bean(UserRepo.class).get(msg.getUsername()).assertion();
                                String str = msg.toString();
                                str = EmojiPattern.matcher(str).replaceAll(match -> {
                                    var name = match.group(1);
                                    var emoji = ThrowingSupplier.fallback(() -> EmojiUtils.getEmoji(name), $ -> null).get();
                                    if (emoji != null)
                                        return emoji.getEmoji();
                                    var results = adapter.getJda().getEmojisByName(name, true);
                                    return Wrap.ofStream(results.stream())
                                            .map(CustomEmoji::getAsMention)
                                            .orElse(match.group(0));
                                });
                                return new DiscordMessageSource(str)
                                        .setDisplayUser(player.getDisplayUser(User.DisplayUser.Type.Discord, User.DisplayUser.Type.Minecraft)
                                                .assertion())
                                        .setAppend(true);
                            })
                            .subscribeData(webhook),
                    // dc -> mc
                    adapter.listenMessages(id)
                            .filterData(msg -> !msg.getAuthor().isBot() && !msg.getContentRaw().isBlank())
                            .mapData(msg -> Tellraw.Command.builder()
                                    .selector(Tellraw.Selector.Base.ALL_PLAYERS)
                                    .component(White.text("<").build())
                                    .component(Dark_Aqua.text(bean(UserRepo.class)
                                                    .findByDiscordId(msg.getAuthor().getIdLong())
                                                    // prefer effective name here; only try minecraft variant now
                                                    .flatMap(usr -> usr.getDisplayUser(User.DisplayUser.Type.Minecraft).wrap())
                                                    .map(User.DisplayUser::username)
                                                    // otherwise use effective name
                                                    .orElseGet(() -> msg.getAuthor().getEffectiveName()))
                                            .hoverEvent(show_text.value("Open in Discord"))
                                            .clickEvent(open_url.value(msg.getJumpUrl()))
                                            .format(Underlined)
                                            .build())
                                    .component(White.text("> ").build())
                                    // todo convert markdown to tellraw data
                                    .component(Reset.text(msg.getContentStripped() + (msg.getAttachments().isEmpty()
                                            ? ""
                                            : msg.getAttachments().stream()
                                            .map(Message.Attachment::getUrl)
                                            .collect(Collectors.joining(" ")))).build())
                                    .build()
                                    .toString())
                            .peekData(log::finest)
                            .subscribeData(tellraw -> console.execute(tellraw).exceptionally(Polyfill.exceptionLogger(log)))
            );
        });

        //moderation channel
        Optional.ofNullable(proto.getModerationChannelId()).ifPresent(id -> {
            final var bot = adapter.messageTemplate(id);

            // public status -> dc
            server.component(StatusModule.class).map(StatusModule::getBus).ifPresent(bus ->
                    addChildren(bus.filterData(msg -> msg.getScope() == IStatusMessage.Scope.Moderation)
                            .mapData(msg -> new EmbedBuilder()
                                    //.setAuthor(parent.getAlternateName(),
                                    //        Optional.ofNullable(parent.getHomepage())
                                    //                .orElse(parent.getViewURL()),
                                    //        parent.getThumbnailURL())
                                    .setDescription(msg.toStatusMessage())
                                    .setColor(msg.getStatus().getColor())
                                    .setFooter(msg.getMessage())
                                    .setTimestamp(Instant.now()))
                            .mapData(DiscordMessageSource::new)
                            .peekData(msg -> msg.setAppend(false))
                            .subscribeData(bot)));
        });

        // console channel
        Optional.ofNullable(proto.getConsoleChannelId()).ifPresent(id -> {
            final var channel = adapter.channelAsStream(id, proto.getFancyConsole());
            addChildren(
                    // mc -> dc
                    console.getBus().subscribeData(channel::println),
                    // dc -> mc
                    adapter.listenMessages(id)
                            .filterData(msg -> !msg.getAuthor().isBot())
                            .mapData(msg -> {
                                var raw = msg.getContentRaw();
                                if (proto.getFancyConsole() && !msg.getAuthor().equals(adapter.getJda().getSelfUser()))
                                    msg.delete().queue();
                                //noinspection RedundantCast //ide error
                                return (String) raw;
                            })
                            .filterData(cmd -> proto.getConsoleChannelPrefix() == null || cmd.startsWith(proto.getConsoleChannelPrefix()))
                            .mapData(cmd -> proto.getConsoleChannelPrefix() == null ? cmd : cmd.substring(proto.getConsoleChannelPrefix().length()))
                            .subscribeData(input -> console.execute(input).exceptionally(Polyfill.exceptionLogger(log)))
            );
        });
    }
}
