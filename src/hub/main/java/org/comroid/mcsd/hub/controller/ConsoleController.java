package org.comroid.mcsd.hub.controller;

import jakarta.servlet.http.HttpSession;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.comroid.api.func.ext.Wrap;
import org.comroid.api.func.util.DelegateStream;
import org.comroid.api.tree.Container;
import org.comroid.mcsd.core.ServerManager;
import org.comroid.mcsd.core.entity.AbstractEntity;
import org.comroid.mcsd.core.entity.server.Server;
import org.comroid.mcsd.core.entity.system.User;
import org.comroid.mcsd.core.exception.EntityNotFoundException;
import org.comroid.mcsd.core.module.console.ConsoleModule;
import org.comroid.mcsd.core.repo.system.UserRepo;
import org.comroid.mcsd.hub.config.WebSocketConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Controller
public class ConsoleController {
    private final Map<User, Connection> connections = new ConcurrentHashMap<>();
    @Autowired
    private SimpMessagingTemplate respond;
    @Autowired
    private UserRepo users;
    @Autowired
    private ServerManager serverManager;

    private User usr(Map<String, Object> attr) {
        var session = (HttpSession) attr.get(WebSocketConfig.HTTP_SESSION_KEY);
        return users.get(session).get();
    }

    private ServerManager.Entry srv(UUID serverId) {
        return serverManager.get(serverId).orElseThrow(() -> new EntityNotFoundException(Server.class, serverId));
    }

    private Wrap<Connection> con(User user) {
        return Wrap.of(connections.getOrDefault(user, null));
    }

    private Connection con(User user, ServerManager.Entry srv) {
        return connections.compute(user, (k, v) -> {
            if (v != null && !v.isClosed())
                v.close();
            var con = new Connection(user, srv);
            con.start();
            return con;
        });
    }

    @MessageMapping("/console/connect")
    @SendToUser("/console/handshake")
    public Server connect(@Header("simpSessionAttributes") Map<String, Object> attr, @Payload UUID serverId) {
        var user = usr(attr);
        var srv = srv(serverId);
        srv.getServer().requirePermission(user, AbstractEntity.Permission.Console);
        var con = con(user, srv);
        return con.entry.getServer();
    }

    @MessageMapping("/console/input")
    @SendToUser("/console/output")
    public String input(@Header("simpSessionAttributes") Map<String, Object> attr, final @Payload String input) {
        var user = usr(attr);
        return con(user).stream().findAny()
                .map(con -> con.console.execute(input))
                .map(CompletableFuture::join)
                .orElse("");
    }

    @MessageMapping("/console/disconnect")
    @SendToUser("/console/goodbye")
    public boolean disconnect(@Header("simpSessionAttributes") Map<String, Object> attr) {
        var user = usr(attr);
        if (!connections.containsKey(user))
            return false;
        return Wrap.of(connections.remove(user))
                .peek(Container.Base::close)
                .isNonNull();
    }

    @Value
    private class Connection extends Container.Base {
        User user;
        ServerManager.Entry entry;
        ConsoleModule<?> console;

        private Connection(User user, ServerManager.Entry entry)
        {
            assert user.getName() != null : "User has no name";

            this.user = user;
            this.entry = entry;
            this.console = entry.component(ConsoleModule.class)
                    .orElseThrow(()->new NoSuchElementException(entry.getServer()+" has no Console Module"));

            addChildren(console.getBus()
                    //.filter(e-> DelegateStream.IO.EventKey_Output.equals(e.getKey()))
                    .mapData(str -> str.replaceAll("((\r?\n)|(br */))+", "<br/>"))
                    .subscribeData(txt -> respond.convertAndSendToUser(user.getName(), "/console/output", txt)));
        }
    }
}
