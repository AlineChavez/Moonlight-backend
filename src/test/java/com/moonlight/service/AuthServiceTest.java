package com.moonlight.service;

import com.moonlight.repository.UserRepository;
import com.moonlight.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(new UserRepository(), new JwtUtil());
    }

    @Test
    void register_shouldReturnTokenAndUser() {
        Map<String, Object> result = authService.register("Juan Pérez", "juan@test.com", "password123");

        assertNotNull(result.get("token"));
        assertNotNull(result.get("user"));

        Map<?, ?> user = (Map<?, ?>) result.get("user");
        assertEquals("Juan Pérez", user.get("name"));
        assertEquals("juan@test.com", user.get("email"));
    }

    @Test
    void register_shouldFailIfEmailAlreadyExists() {
        authService.register("Juan", "duplicado@test.com", "password123");

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                authService.register("Otro", "duplicado@test.com", "password123")
        );

        assertEquals("El email ya está registrado", ex.getMessage());
    }

    @Test
    void login_shouldReturnTokenAfterRegister() {
        authService.register("Maria", "maria@test.com", "password123");
        Map<String, Object> result = authService.login("maria@test.com", "password123");

        assertNotNull(result.get("token"));
        Map<?, ?> user = (Map<?, ?>) result.get("user");
        assertEquals("maria@test.com", user.get("email"));
    }

    @Test
    void login_shouldFailWithWrongPassword() {
        authService.register("Carlos", "carlos@test.com", "password123");

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                authService.login("carlos@test.com", "wrongpassword")
        );

        assertEquals("Credenciales incorrectas", ex.getMessage());
    }

    @Test
    void login_shouldFailWithNonExistentEmail() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                authService.login("noexiste@test.com", "password123")
        );

        assertEquals("Credenciales incorrectas", ex.getMessage());
    }

    @Test
    void getProfile_shouldReturnUserById() {
        Map<String, Object> result = authService.register("Ana", "ana@test.com", "password123");
        Map<?, ?> userMap = (Map<?, ?>) result.get("user");
        Long userId = (Long) userMap.get("id");

        var user = authService.getProfile(userId);
        assertEquals("Ana", user.getName());
    }

    @Test
    void getProfile_shouldFailIfUserNotFound() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                authService.getProfile(999L)
        );
        assertEquals("Usuario no encontrado", ex.getMessage());
    }
}