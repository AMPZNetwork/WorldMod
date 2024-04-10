package org.comroid.mcsd.agent.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.comroid.api.func.util.Streams;
import org.comroid.mcsd.agent.AgentRunner;
import org.comroid.mcsd.core.MCSD;
import org.comroid.mcsd.core.entity.AbstractEntity;
import org.comroid.mcsd.core.entity.server.Server;
import org.comroid.mcsd.core.entity.system.Agent;
import org.comroid.mcsd.core.entity.system.User;
import org.comroid.mcsd.core.exception.InsufficientPermissionsException;
import org.comroid.mcsd.core.repo.server.ServerRepo;
import org.comroid.mcsd.core.repo.system.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Stream.*;
import static org.comroid.mcsd.core.util.ApplicationContextProvider.bean;

@Slf4j
@Controller
@RequestMapping("/api")
public class ApiController {
    @Autowired
    private UserRepo users;
    @Autowired
    private ServerRepo servers;
    @Autowired
    private AgentRunner runner;
    @Autowired
    private Agent me;

    @ResponseBody
    @GetMapping("/webapp/user")
    public User user(HttpSession session) {
        return users.get(session).get();
    }

    @ResponseBody
    @GetMapping("/webapp/servers")
    public List<Server> servers(HttpSession session) {
        return bean(MCSD.class).servers().stream()
                .filter(x->x.hasPermission(user(session)))
                .toList();
    }

    @ResponseBody
    @GetMapping("/webapp/agent")
    public Agent agent(HttpSession session) {
        var user = user(session);
        if (concat(of(me), Streams.of(servers.findAllForAgent(me.getId())))
                .noneMatch(it->it.hasPermission(user)))
            throw new InsufficientPermissionsException(user,me).toStatusCodeExc();
        return me;
    }
}
