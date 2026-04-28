package com.moonlight.resource;

import com.moonlight.model.User;
import com.moonlight.repository.UserRepository;
import com.moonlight.security.JwtUtil;
import com.moonlight.service.AuthService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final AuthService authService = new AuthService(new UserRepository(), new JwtUtil());

    @POST
    @Path("/login")
    public Response login(Map<String, String> body) {
        try {
            String email = body.get("email");
            String password = body.get("password");

            if (email == null || password == null) {
                return Response.status(400)
                        .entity(Map.of("message", "Email y contraseña son requeridos"))
                        .build();
            }

            Map<String, Object> result = authService.login(email, password);
            return Response.ok(result).build();

        } catch (RuntimeException e) {
            return Response.status(401)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/register")
    public Response register(Map<String, String> body) {
        try {
            String name = body.get("name");
            String email = body.get("email");
            String password = body.get("password");

            if (name == null || email == null || password == null) {
                return Response.status(400)
                        .entity(Map.of("message", "Nombre, email y contraseña son requeridos"))
                        .build();
            }

            if (password.length() < 8) {
                return Response.status(400)
                        .entity(Map.of("message", "La contraseña debe tener mínimo 8 caracteres"))
                        .build();
            }

            Map<String, Object> result = authService.register(name, email, password);
            return Response.status(201).entity(result).build();

        } catch (RuntimeException e) {
            return Response.status(400)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/me")
    public Response getProfile(@HeaderParam("Authorization") String authHeader) {
        try {
            JwtUtil jwtUtil = new JwtUtil();
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(401)
                        .entity(Map.of("message", "Token requerido"))
                        .build();
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.isValid(token)) {
                return Response.status(401)
                        .entity(Map.of("message", "Token inválido"))
                        .build();
            }

            Long userId = jwtUtil.getUserId(token);
            User user = authService.getProfile(userId);

            Map<String, Object> userMap = Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail(),
                    "role", user.getRole()
            );

            return Response.ok(userMap).build();

        } catch (RuntimeException e) {
            return Response.status(404)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }
}