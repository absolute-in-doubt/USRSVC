package com.innowise.userservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test configuration that provides a permissive JWT decoder for e2e tests.
 * Token format: "userId.login.role1,role2,..."
 * Example: "1.john.ADMIN,SERVICE"
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return new TestJwtDecoder();
    }

    private static class TestJwtDecoder implements JwtDecoder {
        @Override
        public Jwt decode(String token) throws JwtValidationException {
            // Parse simple token format: "userId.login.role1,role2,..."
            String[] parts = token.split("\\.");
            if (parts.length < 3) {
                throw new JwtValidationException("Invalid test token format", null);
            }
            
            String userId = parts[0];
            String login = parts[1];
            String[] roles = parts[2].split(",");
            
            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", userId);
            claims.put("login", login);
            claims.put("roles", List.of(roles));
            
            Map<String, Object> headers = new HashMap<>();
            headers.put("alg", "none");
            
            return new Jwt(
                token,
                Instant.now(),
                Instant.now().plusSeconds(3600),
                headers,
                claims
            );
        }
    }
}
