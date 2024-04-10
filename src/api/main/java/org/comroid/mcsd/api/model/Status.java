package org.comroid.mcsd.api.model;

import lombok.Getter;
import lombok.Value;
import org.comroid.api.attr.IntegerAttribute;
import org.comroid.api.text.minecraft.McFormatCode;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Objects;

@Getter
public enum Status implements IntegerAttribute, IStatusMessage {
    unknown_status      (Scope.Moderation, "❔", McFormatCode.Dark_Gray),
    offline             (Scope.Public, "❌", McFormatCode.Dark_Red),
    starting            (Scope.Moderation, "⏯️", McFormatCode.Aqua),
    in_maintenance_mode (Scope.Moderation, "\uD83D\uDD27", McFormatCode.Yellow),
    running_backup      (Scope.Moderation, "\uD83D\uDCBE", McFormatCode.Green),
    updating            (Scope.Moderation, "\uD83D\uDD04️", McFormatCode.Light_Purple),
    in_Trouble          (Scope.Moderation, "⚠️", McFormatCode.Gold),
    online              (Scope.Public, "✅", McFormatCode.Dark_Green),
    shutting_down       (Scope.Moderation, "\uD83D\uDED1", McFormatCode.Red);

    private final String emoji;
    private final McFormatCode format;
    private final Scope scope;

    Status(Scope scope, String emoji, McFormatCode format) {
        if (!format.isColor())
            throw new IllegalArgumentException("Invalid format code; must be color: ");
        this.emoji = emoji;
        this.format = format;
        this.scope = scope;
    }

    public Color getColor() {
        return format.getColor();
    }

    @Override
    public String getName() {
        return IntegerAttribute.super.getName().replace('_',' ');
    }

    @Override
    public String getAlternateName() {
        return getEmoji();
    }

    @Override
    public Status getStatus() {
        return this;
    }

    @Value
    @SuppressWarnings("InnerClassMayBeStatic")
    public class Message implements IStatusMessage {
        @Nullable Scope scope;
        @Nullable String message;

        public Message(@Nullable String message) {
            this(null, message);
        }

        public Message(@Nullable Scope scope, @Nullable String message) {
            this.scope = scope;
            this.message = message;
        }

        public Status getStatus() {
            return Status.this;
        }

        public Scope getScope() {
            return Objects.requireNonNullElseGet(scope, () -> getStatus().getScope());
        }
    }
}
