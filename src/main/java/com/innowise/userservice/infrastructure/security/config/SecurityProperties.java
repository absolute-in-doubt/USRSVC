package com.innowise.userservice.infrastructure.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Set;

@ConfigurationProperties(prefix = "application.security")
public record SecurityProperties(
        String jwksUrl,
        Paths paths
) {


    public record Paths(
            Set<String> publicPaths,
            Set<String> userPaths,
            Set<String> adminPaths){}

}

