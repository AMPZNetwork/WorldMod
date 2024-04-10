package org.comroid.mcsd.core.model;

import lombok.Data;
import net.dv8tion.jda.api.EmbedBuilder;
import org.comroid.api.func.ext.StreamSupplier;
import org.comroid.api.func.util.Streams;
import org.comroid.mcsd.core.entity.system.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@Data
public final class DiscordMessageSource implements StreamSupplier<Object> {
    private @Nullable Object data;
    private @Nullable User.DisplayUser displayUser = null;
    private boolean append = false;

    public DiscordMessageSource() {
    }

    public DiscordMessageSource(@Nullable Object data) {
        this.data = data;
    }

    public boolean isEmbed() {
        return embed().isPresent();
    }

    public boolean isString() {
        return string().isPresent();
    }

    @Override
    public Stream<Object> stream() {
        return Optional.ofNullable(data).stream();
    }

    public Optional<String> string() {
        return stream()
                .map(String::valueOf)
                .findAny();
    }

    public Optional<EmbedBuilder> embed() {
        return stream()
                .flatMap(Streams.cast(EmbedBuilder.class))
                .findAny();
    }

    public DiscordMessageSource embed(final Consumer<EmbedBuilder> embedModifier) {
        data = embed().<Object>map(embed -> {
                    embedModifier.accept(embed);
                    embed.setTimestamp(Instant.now());
                    return embed;
                })
                .orElse(data);
        return this;
    }

    public DiscordMessageSource setDisplayUser(User.DisplayUser displayUser) {
        this.displayUser = displayUser;
        return embed(embed -> {
            embed.setAuthor(displayUser.username(), this.displayUser.url(), displayUser.avatarUrl());
        });
    }

    public CompletableFuture<?> send(Sender sender) {
        return send(sender::sendString, sender::sendEmbed);
    }

    public CompletableFuture<?> send(
            final Function<DiscordMessageSource, CompletableFuture<?>> string,
            final Function<DiscordMessageSource, CompletableFuture<?>> embed
    ) {
        if (isEmbed())
            return embed.apply(this);
        if (isString())
            return string.apply(this);
        throw new RuntimeException("invalid state");
    }

    public <T> CompletableFuture<T> execEdit(
            final long messageId,
            final BiFunction<@NotNull Long, String, CompletableFuture<T>> string,
            final BiFunction<@NotNull Long, EmbedBuilder, CompletableFuture<T>> embed
    ) {
        return embed().map(e -> embed.apply(messageId, e))
                .or(() -> string().map(s -> string.apply(messageId, s)))
                .orElseThrow();
    }

    public <T> CompletableFuture<T> execSend(
            final Function<String, CompletableFuture<T>> string,
            final Function<EmbedBuilder, CompletableFuture<T>> embed
    ) {
        return embed().map(embed)
                .or(() -> string().map(string))
                .orElseThrow();
    }

    public interface Sender {
        CompletableFuture<@NotNull Long> sendString(DiscordMessageSource source);

        CompletableFuture<@NotNull Long> sendEmbed(DiscordMessageSource source);
    }
}
