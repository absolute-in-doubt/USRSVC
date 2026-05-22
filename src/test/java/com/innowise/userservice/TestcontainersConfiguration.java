package com.innowise.userservice;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import jakarta.annotation.PreDestroy;

@TestConfiguration(proxyBeanMethods = false)
@Testcontainers
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgresContainer() {
		return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"));
	}

	@Bean
	@ServiceConnection
	RedisContainer redisContainer() {
		RedisContainer container = new RedisContainer(DockerImageName.parse("redis:latest"))
			.withExposedPorts(6379);
		container.start();
		
		// Manually set system properties for Redis connection
		System.setProperty("spring.data.redis.host", container.getHost());
		System.setProperty("spring.data.redis.port", container.getMappedPort(6379).toString());
		
		return container;
	}
	
	@PreDestroy
	public void preDestroy() {
		// Clean up system properties
		System.clearProperty("spring.data.redis.host");
		System.clearProperty("spring.data.redis.port");
	}

}
