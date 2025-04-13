package com.unicolombo.bienestar.services;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.HashMap;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

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
        variables.put("fecha", java.time.LocalDateTime.now());

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

            Context context = new Context();
            context.setVariables(variables);
            String htmlContent = templateEngine.process(template, context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailServiceException("Error al enviar email a: " + to, e);
        }
    }
}