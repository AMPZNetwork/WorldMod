package org.comroid.mcsd.core.config;

import org.comroid.mcsd.api.dto.config.McsdConfig;
import org.comroid.mcsd.core.model.ModuleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests()
                .requestMatchers(AntPathRequestMatcher.antMatcher("/api/open/**")).permitAll()
                .anyRequest().authenticated()
                .and()
                .csrf().disable()
                .oauth2Login().and()
                .build();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(@Autowired ModuleType.Side side, @Autowired McsdConfig config) {
        return new InMemoryClientRegistrationRepository(config.getOAuth().stream()
                .map(info -> ClientRegistration.withRegistrationId(info.getName())
                        .clientId(info.getClientId())
                        .clientSecret(info.getSecret())
                        .scope(info.getScope())
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .redirectUri(switch (side) {
                            case Hub -> info.getRedirectUrl();
                            case Agent -> info.getAgentRedirectUrl();
                        })
                        .authorizationUri(info.getAuthorizationUrl())
                        .tokenUri(info.getTokenUrl())
                        .userInfoUri(info.getUserInfoUrl())
                        .userNameAttributeName(info.getUserNameAttributeName())
                        .build())
                .toArray(ClientRegistration[]::new));
    }
}
