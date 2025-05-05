package com.unicolombo.bienestar.auth;

import com.unicolombo.bienestar.controllers.AuthController;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.services.AuthService;
import com.unicolombo.bienestar.services.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post; // ✅ Importación correcta
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

    @Test
    void login_ValidRequest_ReturnsJwtToken() throws Exception {
        when(authService.authenticate(anyString(), anyString()))
                .thenReturn(new Usuario());

        when(jwtService.generateToken(any(Usuario.class)))
                .thenReturn("fake-jwt-token");

        mockMvc.perform(post("/api/auth/login") // ✅ Ahora funciona
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "email": "test@unicolombo.edu.co",
                        "password": "password123"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                .andExpect(jsonPath("$.usuario.email").exists());
    }
}