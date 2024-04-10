package org.comroid.mcsd.hub.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    public static final String HTTP_SESSION_KEY = "HTTP_SESSION";
    public static final String USER_ID_KEY = "USER_ID";

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
        registry.addEndpoint("/connector")
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
}
