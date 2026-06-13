package com.innowise.userservice.infrastructure.security.config;

import com.innowise.userservice.infrastructure.security.converter.JwtConverter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.AntPathMatcher;

@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityProperties properties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtConverter jwtAuthenticationConverter,
                                           BearerTokenResolver publicPathsBearerTokenResolver
    ) throws Exception {

        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .headers(Customizer.withDefaults())
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .servletApi(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(properties.paths().publicPaths().toArray(String[]::new)).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(publicPathsBearerTokenResolver)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                )
                .anonymous(Customizer.withDefaults())
                .exceptionHandling(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(properties.jwksUrl()).build();
    }

    @Bean
    public BearerTokenResolver publicPathsBearerTokenResolver() {
        DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();
        AntPathMatcher matcher = new AntPathMatcher();
        return request -> {
            boolean isPublic = properties.paths().publicPaths().stream().anyMatch(p -> matcher.match(p, request.getServletPath()));
            return isPublic ? null : delegate.resolve(request);
        };
    }
}
