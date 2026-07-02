package com.moonlight.repository;

import com.moonlight.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    User save(User user);
    boolean existsByEmail(String email);
    List<User> findAll();
}
