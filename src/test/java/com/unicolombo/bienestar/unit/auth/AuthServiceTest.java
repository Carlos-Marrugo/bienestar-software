package com.unicolombo.bienestar.unit.auth;

import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.Role;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import com.unicolombo.bienestar.services.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticate_ValidAdminCredentials_ReturnsUser() {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setEmail("admin@unicolombo.edu.co");
        usuarioMock.setPassword("$2a$10$encoded");
        usuarioMock.setRol(Role.ADMIN);

        when(usuarioRepository.findByEmail("admin@unicolombo.edu.co"))
                .thenReturn(Optional.of(usuarioMock));

        when(passwordEncoder.matches("admin123", usuarioMock.getPassword()))
                .thenReturn(true);

        Usuario result = authService.authenticate("admin@unicolombo.edu.co", "admin123");

        assertNotNull(result);
        assertEquals(Role.ADMIN, result.getRol());
        verify(usuarioRepository, times(1)).findByEmail(anyString());
    }

    @Test
    void authenticate_InvalidPassword_ThrowsException() {
        when(usuarioRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(new Usuario()));

        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        assertThrows(BusinessException.class, () -> {
            authService.authenticate("user@test.com", "wrongpass");
        });
    }
}