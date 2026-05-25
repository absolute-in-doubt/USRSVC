package com.innowise.userservice;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class UserServiceApplication {

	@Autowired
	private Environment environment;

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
		System.out.println("""
				     - JSON spec: GET http://localhost:8080/v3/api-docs
				     - YAML spec: GET http://localhost:8080/v3/api-docs.yaml
				     - Swagger UI: GET http://localhost:8080/swagger-ui.html
				""");

		String redisHost = System.getProperty("spring.data.redis.host");
		log.info("Redis host is set to: {}", redisHost);
	}
}
