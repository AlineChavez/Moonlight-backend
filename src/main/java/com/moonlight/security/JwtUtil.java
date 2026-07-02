package com.moonlight.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.logging.Logger;

public class JwtUtil {

    private static final Logger LOGGER = Logger.getLogger(JwtUtil.class.getName());
    private static final long EXPIRATION = 86400000L;

    // No hardcoded fallback: a secret baked into source code is public the moment
    // the repo is. Without JWT_SECRET we generate a random key once per process
    // (static, not per-instance - every resource class creates its own JwtUtil, and
    // they all need to agree on the same key or tokens signed by one won't validate
    // against another). This just means existing tokens are invalidated on restart,
    // which is acceptable since all app state is already in-memory and lost on restart anyway.
    private static final Key KEY = buildKey();

    private static Key buildKey() {
        String envSecret = System.getenv("JWT_SECRET");
        if (envSecret != null && !envSecret.isBlank()) {
            if (envSecret.length() < 32) {
                throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres");
            }
            return Keys.hmacShaKeyFor(envSecret.getBytes());
        }
        LOGGER.warning("JWT_SECRET no está configurado: se generó una clave aleatoria para este proceso. " +
                "Todas las sesiones se invalidarán al reiniciar. Configure JWT_SECRET en producción.");
        return Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    public String generateToken(Long userId, String email, String role) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    public String getEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}