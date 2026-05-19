package com.innowise.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
		System.out.println("""
				     - JSON spec: GET http://localhost:8080/v3/api-docs
				     - YAML spec: GET http://localhost:8080/v3/api-docs.yaml
				     - Swagger UI: GET http://localhost:8080/swagger-ui.html
				""");
	}

}
