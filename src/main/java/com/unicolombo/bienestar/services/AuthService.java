package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private JwtService jwtService;

    @Value("${app.frontend-url:http://localhost:8080}")
    private String frontendUrl;

    @Autowired
    public AuthService(EmailService emailService){
        this.emailService = emailService;
    }

    public Usuario authenticate(String email, String credencial) {
        validarCorreoInstitucional(email);
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Credenciales inválidas"));

        if (!usuario.isActivo()) {
            throw new BusinessException("Usuario inactivo");
        }

        switch(usuario.getRol()) {
            case ESTUDIANTE:
                if (usuario.getEstudiante() == null ||
                        !usuario.getEstudiante().getCodigoEstudiantil().equals(credencial)) {
                    throw new BusinessException("Código estudiantil incorrecto");
                }
                break;

            case ADMIN:
            case INSTRUCTOR:
                if (!passwordEncoder.matches(credencial, usuario.getPassword())) {
                    throw new BusinessException("Contraseña incorrecta");
                }
                break;

            default:
                throw new BusinessException("Rol no soportado para autenticación");
        }

        try {
            emailService.sendLoginNotification(
                    usuario.getEmail(),
                    usuario.getNombre(),
                    usuario.getRol().name()
            );
        } catch (Exception e) {
            log.error("Error al enviar notificación de login: {}", e.getMessage());
        }

        return usuario;
    }

    public Usuario authenticateEstudiante(String email, String codigoEstudiantil) {
        validarCorreoInstitucional(email);
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (usuario.getEstudiante() == null ||
                !usuario.getEstudiante().getCodigoEstudiantil().equals(codigoEstudiantil)) {
            throw new RuntimeException("Código estudiantil incorrecto");
        }

        return usuario;
    }

    public void sendResetPasswordEmail(String email) {
        try {
            log.info("Iniciando proceso de restablecimiento de contraseña para: {}", email);
            validarCorreoInstitucional(email);

            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException("No existe un usuario con ese correo"));

            String token = jwtService.generateToken(usuario);
            String resetUrl = frontendUrl + "/reset-password?token=" + token;
            log.info("URL de restablecimiento generada: {}", resetUrl);

            emailService.sendPasswordResetEmail(email, usuario.getNombre(), resetUrl);

            log.info("Correo de restablecimiento enviado correctamente a: {}", email);
        } catch (BusinessException e) {
            log.error("Error de negocio al procesar solicitud de restablecimiento: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error al enviar correo de restablecimiento: {}", e.getMessage(), e);
            throw new BusinessException("No se pudo enviar el correo de restablecimiento. Por favor, intente más tarde.");
        }
    }

    private void validarCorreoInstitucional(String email) {
        if (!email.endsWith("@unicolombo.edu.co")) {
            throw new BusinessException("Solo se permiten correos institucionales");
        }
    }
}