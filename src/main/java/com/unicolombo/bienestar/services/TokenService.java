package com.unicolombo.bienestar.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class TokenService {

    private final String SECRET = "clave_secreta_segura";

    public String createPasswordResetToken(String email, int minutes) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + minutes * 60 * 1000);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("type", "RESET_PASSWORD")
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }
}
