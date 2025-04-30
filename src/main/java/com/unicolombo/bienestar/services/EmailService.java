package com.unicolombo.bienestar.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.AddressException;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendWelcomeEmail(String toEmail, String nombre, String codigoEstudiantil) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", nombre);
        variables.put("codigo", codigoEstudiantil);

        sendTemplateEmail(
                toEmail,
                "¡Bienvenido al Sistema de Bienestar!",
                "emails/welcome-student",
                variables
        );
    }

    public void sendLoginNotification(String toEmail, String nombre, String rol) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", nombre);
        variables.put("rol", rol);
        variables.put("fecha", LocalDateTime.now());

        sendTemplateEmail(
                toEmail,
                "Notificación de inicio de sesión",
                "emails/login-notification",
                variables
        );
    }

    public void sendInstructorAssignment(String toEmail, String nombreInstructor, String actividadNombre) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("instructorName", nombreInstructor);
        variables.put("actividadNombre", actividadNombre);

        sendTemplateEmail(
                toEmail,
                "Has sido asignado a una nueva actividad",
                "emails/instructor-assignment",
                variables
        );
    }

    private void sendTemplateEmail(String to, String subject, String template, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Configuración explícita del remitente
            InternetAddress fromAddress = new InternetAddress(fromEmail, "Sistema de Bienestar Unicolombo");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(template, context);

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Correo enviado exitosamente a: {}", to);
        } catch (AddressException e) {
            log.error("Error en la dirección de correo: {}", e.getMessage());
            throw new EmailServiceException("Dirección de correo inválida", e);
        } catch (MessagingException e) {
            log.error("Error al enviar correo a {}: {}", to, e.getMessage());
            throw new EmailServiceException("Error al enviar el correo electrónico", e);
        } catch (Exception e) {
            log.error("Error inesperado al enviar correo: {}", e.getMessage());
            throw new EmailServiceException("Error interno del servidor", e);

        }
    }

}