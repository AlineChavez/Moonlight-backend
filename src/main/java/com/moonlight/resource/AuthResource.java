package com.moonlight.resource;

import com.moonlight.exception.ApiException;
import com.moonlight.model.User;
import com.moonlight.repository.JpaUserRepository;
import com.moonlight.repository.UserRepository;
import com.moonlight.security.AuthContext;
import com.moonlight.security.JwtUtil;
import com.moonlight.security.RateLimiter;
import com.moonlight.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.regex.Pattern;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    // 8 attempts per 5 minutes per client IP, to slow down credential-stuffing/brute force.
    private static final RateLimiter LOGIN_RATE_LIMITER = new RateLimiter(8, 5 * 60 * 1000L);

    private static final UserRepository userRepository = new JpaUserRepository();
    private static final JwtUtil jwtUtil = new JwtUtil();
    private static final AuthService authService = new AuthService(userRepository, jwtUtil);
    private final AuthContext authContext = new AuthContext(jwtUtil);

    @POST
    @Path("/login")
    public Response login(Map<String, String> body, @Context HttpServletRequest request) {
        if (!LOGIN_RATE_LIMITER.allow(clientIp(request))) {
            throw ApiException.tooManyRequests("Demasiados intentos, intenta de nuevo más tarde");
        }

        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            throw ApiException.badRequest("Email y contraseña son requeridos");
        }

        Map<String, Object> result = authService.login(email.trim(), password);
        return Response.ok(result).build();
    }

    @POST
    @Path("/register")
    public Response register(Map<String, String> body) {
        String name = body.get("name");
        String email = body.get("email");
        String password = body.get("password");

        if (name == null || name.isBlank() || email == null || password == null) {
            throw ApiException.badRequest("Nombre, email y contraseña son requeridos");
        }

        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw ApiException.badRequest("El email no tiene un formato válido");
        }

        if (password.length() < 8) {
            throw ApiException.badRequest("La contraseña debe tener mínimo 8 caracteres");
        }

        Map<String, Object> result = authService.register(name.trim(), email.trim(), password);
        return Response.status(201).entity(result).build();
    }

    @GET
    @Path("/me")
    public Response getProfile(@HeaderParam("Authorization") String authHeader) {
        Long userId = authContext.requireUserId(authHeader);
        User user = authService.getProfile(userId);

        Map<String, Object> userMap = Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole()
        );

        return Response.ok(userMap).build();
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
