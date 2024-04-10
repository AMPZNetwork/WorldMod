package org.comroid.mcsd.agent.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.comroid.mcsd.agent.controller.ConsoleController;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

@Slf4j
@Deprecated
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketHandler implements WebSocketMessageBrokerConfigurer {
    public static final String HTTP_SESSION_KEY = "HTTP_SESSION";
    public static final String USER_ID_KEY = "USER_ID";
    @Lazy @Autowired
    private ConsoleController consoleController;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/console")
                .addInterceptors(httpSessionHandshakeInterceptor())
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Bean
    public HttpSessionHandshakeInterceptor httpSessionHandshakeInterceptor() {
        return new HttpSessionHandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(
                    @NotNull ServerHttpRequest request,
                    @NotNull ServerHttpResponse response,
                    @NotNull WebSocketHandler wsHandler,
                    @NotNull Map<String, Object> attributes
            ) throws Exception {
                HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
                HttpSession httpSession = servletRequest.getSession();
                attributes.put(HTTP_SESSION_KEY, httpSession);
                return super.beforeHandshake(request, response, wsHandler, attributes);
            }
        };
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        var con = consoleController.con(session.getAttributes());
        con.close();
        super.afterConnectionClosed(session, status);
    }
}
