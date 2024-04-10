package org.comroid.mcsd.api.dto.comm;

import lombok.Value;
import org.comroid.api.data.seri.DataNode;
import org.comroid.api.func.util.Bitmask;
import org.comroid.api.text.Markdown;

@Value
public class PlayerEvent implements DataNode {
    String username;
    String message;
    Type type;

    public String toString() {
        return type == Type.Chat ? message : Markdown.Quote.apply(message);
    }

    public enum Type implements Bitmask.Attribute<Type> {
        Other, JoinLeave, Achievement, Death, Chat
    }
}
