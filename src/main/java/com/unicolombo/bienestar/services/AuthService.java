package com.unicolombo.bienestar.services;

import com.unicolombo.bienestar.exceptions.BusinessException;
import com.unicolombo.bienestar.models.Usuario;
import com.unicolombo.bienestar.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final EmailService emailService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private EmailTemplateService emailTemplateService;
    @Autowired
    private JwtService jwtService;

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

        emailService.sendLoginNotification(
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getRol().name()
        );

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
        validarCorreoInstitucional(email);
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No existe un usuario con ese correo"));

        String token = jwtService.generateToken(usuario);
        String resetUrl = "https://frontend.unicolombo.edu.co/reset-password?token=" + token;

        String subject = "Restablece tu contraseña - Bienestar Unicolombo";
        String body = emailTemplateService.buildResetPasswordEmail(usuario.getNombre(), resetUrl);

        emailService.sendWelcomeEmail(email, subject, body);
    }

    private void validarCorreoInstitucional(String email) {
        if (!email.endsWith("@unicolombo.edu.co")) {
            throw new BusinessException("Solo se permiten correos institucionales");
        }
    }
}