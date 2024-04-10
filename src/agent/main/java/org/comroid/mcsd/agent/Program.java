package org.comroid.mcsd.agent;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.common.aliasing.qual.Unique;
import org.comroid.api.Polyfill;
import org.comroid.api.func.util.Debug;
import org.comroid.api.func.util.Streams;
import org.comroid.api.info.Log;
import org.comroid.api.net.REST;
import org.comroid.api.os.OS;
import org.comroid.mcsd.agent.config.WebSocketConfig;
import org.comroid.mcsd.agent.controller.ApiController;
import org.comroid.mcsd.api.dto.config.AgentInfo;
import org.comroid.mcsd.api.dto.config.McsdConfig;
import org.comroid.mcsd.core.MCSD;
import org.comroid.mcsd.core.ServerManager;
import org.comroid.mcsd.core.entity.server.Server;
import org.comroid.mcsd.core.entity.system.Agent;
import org.comroid.mcsd.core.exception.EntityNotFoundException;
import org.comroid.mcsd.core.model.ModuleType;
import org.comroid.mcsd.core.repo.server.ServerRepo;
import org.comroid.mcsd.core.repo.system.AgentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.comroid.mcsd.core.util.ApplicationContextProvider.bean;
import static org.comroid.mcsd.core.util.ApplicationContextProvider.wrap;

@Slf4j
@ImportResource({"classpath:beans.xml"})
@SpringBootApplication(scanBasePackages = "org.comroid.mcsd.*")
@ComponentScan(basePackageClasses = {AgentRunner.class, ApiController.class, WebSocketConfig.class})
public class Program implements ApplicationRunner {
    public static void main(String[] args) {
        // todo experimental: is this even still necessary with the agentrunner removed?
        //if (!Debug.isDebug() && !OS.isUnix)
        //    throw new RuntimeException("Only Unix operation systems are supported");
        SpringApplication.run(Program.class, args);
    }

    @Bean
    public ModuleType.Side side() {
        return ModuleType.Side.Agent;
    }

    @Bean
    @Deprecated
    @SneakyThrows
    public AgentInfo agentInfo(@Autowired McsdConfig config) {
        return config.getAgent();
    }

    @Bean
    public Agent me(@Autowired AgentInfo agentInfo, @Autowired AgentRepo agents) {
        return agents.findById(agentInfo.getAgent())
                .orElseThrow(() -> new EntityNotFoundException(Agent.class, agentInfo.getAgent()));
    }

    @Bean
    @Unique
    @Lazy(false)
    @DependsOn("migrateEntities")
    public List<Server> servers(@Autowired ServerRepo serverRepo, @Autowired Agent me) {
        return Streams.of(serverRepo.findAllForAgent(me.getId())).toList();
    }

    @Bean
    public CompletableFuture<?> hubConnect(McsdConfig config) {
        var agent = Objects.requireNonNull(config.getAgent(), "Config is incomplete; agent is missing");

        return REST.request(REST.Method.GET, config.getHubBaseUrl() + "/api/open/agent/hello/"
                        + bean(Agent.class, "me").getId()
                        + (Optional.ofNullable(agent.getBaseUrl())
                        .or(() -> wrap(OS.Host.class, "hostname")
                                .map(OS.Host::name)
                                .map(MCSD::wrapHostname))
                        .map(baseUrl -> "?baseUrl=" + baseUrl)
                        .orElse("")))
                .addHeader("Authorization", agent.getToken())
                .execute()
                .thenApply(REST.Response::validate2xxOK)
                .exceptionally(Polyfill.exceptionLogger(Log.get(), "Could not connect to Hub at " + config.getHubBaseUrl()));
    }

    @Override
    public void run(ApplicationArguments args) {
        var config = bean(McsdConfig.class, "config");
        var agent = Objects.requireNonNull(config.getAgent(), "Config is incomplete; agent is missing");

        bean(ServerManager.class).startAll(bean(List.class, "servers"));
    }
}

