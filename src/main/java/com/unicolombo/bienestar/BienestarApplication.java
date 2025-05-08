package com.unicolombo.bienestar;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication

@OpenAPIDefinition(
		info = @Info(
				title = "Bienestar API",
				version = "1.0",
				description = "API para gestión de actividades deportivas"
		)
)

public class BienestarApplication {

	public static void main(String[] args) {

		SpringApplication.run(BienestarApplication.class, args);
	}

}
