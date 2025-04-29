package com.unicolombo.bienestar.services;

import io.jsonwebtoken.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailTemplateService {

    @Autowired
    private ResourceLoader resourceLoader;

    public String loadResetPasswordTemplate(String nombre, String url) {
        try {
            Resource resource = resourceLoader.getResource("classpath:templates.emails/reset-password.html");
            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            return content.replace("{{nombre}}", nombre)
                    .replace("{{url}}", url);
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la plantilla de correo", e);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }
}
