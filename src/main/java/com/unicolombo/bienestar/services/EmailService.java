package com.unicolombo.bienestar.services;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendRegistrationEmail(String toEmail, String nombreUsuario) {
        Map<String, Object> variables = Map.of("name", nombreUsuario);
        sendTemplateEmail(
                toEmail,
                "Bienvenido a Bienestar Universitario",
                "emails/welcome",
                variables
        );
    }

    public void sendInstructorAssignment(String toEmail, String instructorName, String actividadNombre) {
        Map<String, Object> variables = Map.of(
                "instructorName", instructorName,
                "actividadNombre", actividadNombre
        );
        sendTemplateEmail(
                toEmail,
                "Nueva asignación como instructor",
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