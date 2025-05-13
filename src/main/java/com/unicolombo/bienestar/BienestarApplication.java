package com.unicolombo.bienestar;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.unicolombo.bienestar.repositories")
@EntityScan(basePackages = "com.unicolombo.bienestar.models")
public class BienestarApplication {

	public static void main(String[] args) {

		SpringApplication.run(BienestarApplication.class, args);
	}

}
