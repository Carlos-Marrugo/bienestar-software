package com.unicolombo.bienestar.auth;

import com.unicolombo.bienestar.dto.LoginRequest;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeAll
    void setup() {
        // Prepara datos iniciales en H2
        Usuario admin = new Usuario();
        admin.setEmail("admin@unicolombo.edu.co");
        admin.setPassword("$2a$10$encoded"); // Password ya encriptado
        usuarioRepository.save(admin);
    }

    @Test
    void login_WithRealCredentials_ReturnsJwtToken() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/auth/login",
                new LoginRequest(),
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("token"));
    }
}