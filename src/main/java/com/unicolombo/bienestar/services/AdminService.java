package com.unicolombo.bienestar.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AdminService {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean authenticateAdmin(String username, String password) {
        return ADMIN_USERNAME.equals(username) && passwordEncoder.matches(password, passwordEncoder.encode(ADMIN_PASSWORD));
    }
}
