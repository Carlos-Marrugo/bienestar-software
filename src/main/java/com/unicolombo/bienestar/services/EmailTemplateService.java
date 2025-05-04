package com.unicolombo.bienestar.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailTemplateService {

    private final TemplateEngine templateEngine;

    @Autowired
    public EmailTemplateService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String buildResetPasswordEmail(String nombre, String url) {
        Context context = new Context();
        context.setVariable("nombre", nombre);
        context.setVariable("url", url);
        return templateEngine.process("emails/reset-password", context);
    }
}