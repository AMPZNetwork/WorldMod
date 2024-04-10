package org.comroid.mcsd.hub;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.common.aliasing.qual.Unique;
import org.comroid.api.func.util.Streams;
import org.comroid.mcsd.api.dto.config.McsdConfig;
import org.comroid.mcsd.core.ServerManager;
import org.comroid.mcsd.core.entity.server.Server;
import org.comroid.mcsd.core.model.ModuleType;
import org.comroid.mcsd.core.repo.server.ServerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.comroid.mcsd.core.util.ApplicationContextProvider.bean;

@Slf4j
//@EnableWebMvc
@ImportResource({"classpath:beans.xml"})
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class, scanBasePackages = "org.comroid.mcsd.*")
public class Program implements ApplicationRunner {
    public static void main(String[] args) {
        SpringApplication.run(Program.class, args);
    }

    @Bean
    public ModuleType.Side side() {
        return ModuleType.Side.Hub;
    }

    @Bean
    @Unique
    @Lazy(false)
    @DependsOn("migrateEntities")
    public List<Server> servers(@Autowired ServerRepo serverRepo) {
        return Streams.of(serverRepo.findAll()).toList();
    }

    @Bean
    public CompletableFuture<?> hubConnect() {
        // faking it here bcs we ARE the hub
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void run(ApplicationArguments args) {
        var config = bean(McsdConfig.class, "config");

        bean(ServerManager.class).startAll(bean(List.class, "servers"));
    }
}

