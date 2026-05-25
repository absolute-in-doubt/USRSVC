package com.innowise.userservice;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;



@TestConfiguration(proxyBeanMethods = false)
@TestPropertySource(locations = "classpath:application-test.yaml")
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
		return container;
	}
}

