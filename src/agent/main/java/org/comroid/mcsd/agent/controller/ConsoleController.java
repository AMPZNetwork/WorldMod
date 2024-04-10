package org.comroid.mcsd.agent.controller;

import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.comroid.api.func.util.DelegateStream;
import org.comroid.api.func.util.Event;
import org.comroid.mcsd.agent.AgentRunner;
import org.comroid.mcsd.agent.config.WebSocketConfig;
import org.comroid.mcsd.core.entity.server.Server;
import org.comroid.mcsd.core.entity.system.User;
import org.comroid.mcsd.core.module.local.LocalExecutionModule;
import org.comroid.mcsd.core.repo.system.UserRepo;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Deprecated
@Controller
public class ConsoleController {
    private final Map<UUID, Connection> connections = new ConcurrentHashMap<>();
    @Autowired
    private SimpMessagingTemplate respond;
    @Autowired
    private UserRepo users;
    @Autowired
    private AgentRunner agentRunner;

    public Connection con(Map<String, Object> attr) {
        return con(user(attr));
    }
    public Connection con(final User user) {
        return connections.computeIfAbsent(user.getId(), $->new Connection(user));
    }
    public User user(Map<String, Object> attr) {
        var session = (HttpSession) attr.get(WebSocketConfig.HTTP_SESSION_KEY);
        return users.get(session).get();
    }

    @MessageMapping("/console/connect")
    public void connect(@Header("simpSessionAttributes") Map<String, Object> attr) {
        var user = user(attr);
        var con = con(user);
        respond.convertAndSendToUser(user.getName(), "/console/handshake", "");
    }

    @MessageMapping("/console/input")
    public void input(@Header("simpSessionAttributes") Map<String, Object> attr, @Payload String input) {
        var user = user(attr);
        var con = con(user);
        var cmd = input.substring(2, input.length() - 1);
        con.publish("stdout", "> "+cmd+'\n');
        agentRunner.execute(cmd, con);
    }

    @MessageMapping("/console/disconnect")
    public void disconnect(@Header("simpSessionAttributes") Map<String, Object> attr) {
        var user = user(attr);
        con(user).close();
    }

    @Getter
    public class Connection extends Event.Bus<String> {
        @Language("html")
        public static final String br = "<br/>";
        private final User user;
        private @Nullable Server process;

        private Connection(User user) {
            this.user = user;

            agentRunner.oe.redirectToEventBus(this);
        }

        public void attach(Server process) {
            this.process = process;
            process.component(LocalExecutionModule.class).assertion().getOe().redirect(agentRunner.oe);
        }

        public void detach() {
            if (process == null)
                return;
            agentRunner.oe.detach();
            process = null;
        }

        @Event.Subscriber(DelegateStream.IO.EventKey_Output)
        public void handleStdout(Event<String> e) {
            respond.convertAndSendToUser(user.getName(), "/console/output",
                    e.getData().replace("<","&lt;")
                            .replace(">","&gt;")
                            .replaceAll("\r?\n",br));
        }

        @Event.Subscriber(DelegateStream.IO.EventKey_Error)
        public void handleStderr(Event<String> e) {
            respond.convertAndSendToUser(user.getName(), "/console/error",
                    e.getData().replace("<","&lt;")
                            .replace(">","&gt;")
                            .replaceAll("\r?\n",br));
        }

        @Override
        @SneakyThrows
        public void closeSelf() {
            detach();
            connections.remove(user.getId());
            respond.convertAndSendToUser(user.getName(), "/console/disconnect", "");
            super.closeSelf();
        }
    }
}
