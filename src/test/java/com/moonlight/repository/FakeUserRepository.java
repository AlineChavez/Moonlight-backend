package com.moonlight.repository;

import com.moonlight.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory stand-in for JpaUserRepository, used by service unit tests so they
 * don't need a real Postgres instance.
 */
public class FakeUserRepository implements UserRepository {

    private static final List<User> users = new ArrayList<>();
    private static final AtomicLong counter = new AtomicLong(1);

    @Override
    public Optional<User> findByEmail(String email) {
        return users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public Optional<User> findById(Long id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(counter.getAndIncrement());
            users.add(user);
        } else {
            users.removeIf(u -> u.getId().equals(user.getId()));
            users.add(user);
        }
        return user;
    }

    @Override
    public boolean existsByEmail(String email) {
        return users.stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users);
    }
}
