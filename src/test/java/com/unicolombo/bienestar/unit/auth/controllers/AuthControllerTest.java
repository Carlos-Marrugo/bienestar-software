package com.unicolombo.bienestar.unit.auth.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicolombo.bienestar.controllers.AuthController;
import com.unicolombo.bienestar.models.RefreshToken;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.InstructorRepository;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import com.unicolombo.bienestar.services.AuthService;
import com.unicolombo.bienestar.services.JwtService;
import com.unicolombo.bienestar.services.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private RefreshTokenService refreshTokenService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private InstructorRepository instructorRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    void login_ValidRequest_ReturnsJwtToken() throws Exception {
        // Preparar usuario mock
        Usuario usuario = new Usuario();
        usuario.setEmail("test@unicolombo.edu.co");

        // Preparar refresh token mock
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("fake-refresh-token");
        refreshToken.setUsuario(usuario);

        // Configurar comportamiento de los mocks
        when(authService.authenticate(anyString(), anyString())).thenReturn(usuario);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn("fake-jwt-token");
        when(refreshTokenService.createRefreshToken(any(Usuario.class))).thenReturn(refreshToken);
        when(instructorRepository.findByUsuarioEmail(anyString())).thenReturn(Optional.empty());

        // Ejecutar la prueba
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "email": "test@unicolombo.edu.co",
                        "password": "password123"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("fake-refresh-token"));
    }
}
