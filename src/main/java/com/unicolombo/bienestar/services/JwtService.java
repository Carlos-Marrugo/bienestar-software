package com.unicolombo.bienestar.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret:S2RjYmxFM2ZsYVZXaWRTa0VMMEdmWjZYZUV4dzRjY0NacmlvT0VnT2NVRWlDTzNv}")
    private String secretKey;

    @Value("${app.jwt.expirationMs:86400000}") // 24 horas por defecto
    private long jwtExpirationMs;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String renewTokenFromOldToken(String oldToken, UserDetails userDetails) {
        try {
            Claims claims = extractAllClaims(oldToken);

            Map<String, Object> extraClaims = new HashMap<>();
            // Solo copiamos claims seguros, evitando los que son manejados por JWT
            for (Map.Entry<String, Object> entry : claims.entrySet()) {
                if (!entry.getKey().equals(Claims.EXPIRATION) &&
                        !entry.getKey().equals(Claims.ISSUED_AT) &&
                        !entry.getKey().equals(Claims.SUBJECT)) {
                    extraClaims.put(entry.getKey(), entry.getValue());
                }
            }

            return generateToken(extraClaims, userDetails);
        } catch (Exception e) {
            // Si hay algún problema con el token antiguo, generamos uno nuevo desde cero
            return generateToken(userDetails);
        }
    }
}