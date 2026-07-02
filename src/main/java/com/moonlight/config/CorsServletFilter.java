package com.moonlight.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Single source of truth for CORS + baseline security response headers.
 *
 * This used to be duplicated across a servlet filter and two JAX-RS level filters,
 * which risked emitting the Access-Control-Allow-Origin header twice on the same
 * response (browsers reject that). Keeping exactly one filter, mapped to /*, also
 * guarantees CORS headers are present on errors that never reach JAX-RS (e.g. a
 * container-level 404/500).
 */
public class CorsServletFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(CorsServletFilter.class.getName());

    // Comma-separated list of allowed origins, e.g. "https://moonlight-cafe.vercel.app,https://moonlight.com"
    // Falls back to "*" (previous behavior) if unset, since the API uses Authorization headers
    // rather than cookies (Allow-Credentials stays false either way).
    private static final List<String> ALLOWED_ORIGINS = resolveAllowedOrigins();

    private static List<String> resolveAllowedOrigins() {
        String configured = System.getenv("ALLOWED_ORIGINS");
        if (configured == null || configured.isBlank()) {
            LOGGER.warning("ALLOWED_ORIGINS no está configurado: se permitirá cualquier origen (*). " +
                    "Defina ALLOWED_ORIGINS (lista separada por comas) en producción.");
            return List.of("*");
        }
        return Arrays.stream(configured.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        applyCors(request, response);

        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(req, res);
    }

    private void applyCors(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");

        if (ALLOWED_ORIGINS.contains("*")) {
            response.setHeader("Access-Control-Allow-Origin", "*");
        } else if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Vary", "Origin");
        }

        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "false");
        response.setHeader("Access-Control-Max-Age", "86400");
    }
}
