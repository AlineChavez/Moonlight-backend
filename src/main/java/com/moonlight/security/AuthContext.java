package com.moonlight.security;

import com.moonlight.exception.ApiException;

/**
 * Central place to turn an Authorization header into a verified user id / role,
 * so resources don't each re-implement token parsing and access checks.
 */
public class AuthContext {

    public static final String ADMIN_ROLE = "ADMIN";

    private final JwtUtil jwtUtil;

    public AuthContext(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public Long requireUserId(String authHeader) {
        return jwtUtil.getUserId(extractToken(authHeader));
    }

    /**
     * Validates the token belongs to an ADMIN and returns that user's id.
     */
    public Long requireAdmin(String authHeader) {
        String token = extractToken(authHeader);
        if (!ADMIN_ROLE.equals(jwtUtil.getRole(token))) {
            throw ApiException.forbidden("Se requieren permisos de administrador");
        }
        return jwtUtil.getUserId(token);
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw ApiException.unauthorized("Token requerido");
        }
        String token = authHeader.substring(7).trim();
        if (token.isEmpty() || !jwtUtil.isValid(token)) {
            throw ApiException.unauthorized("Token inválido o expirado");
        }
        return token;
    }
}
