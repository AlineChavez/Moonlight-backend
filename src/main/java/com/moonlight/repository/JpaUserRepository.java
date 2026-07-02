package com.moonlight.repository;

import com.moonlight.model.User;
import com.moonlight.persistence.JpaUtil;

import java.util.List;
import java.util.Optional;

public class JpaUserRepository implements UserRepository {

    @Override
    public Optional<User> findByEmail(String email) {
        return JpaUtil.doInTransaction(em ->
                em.createQuery("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)", User.class)
                        .setParameter("email", email)
                        .getResultStream()
                        .findFirst());
    }

    @Override
    public Optional<User> findById(Long id) {
        return JpaUtil.doInTransaction(em -> Optional.ofNullable(em.find(User.class, id)));
    }

    @Override
    public User save(User user) {
        return JpaUtil.doInTransaction(em -> {
            if (user.getId() == null) {
                em.persist(user);
                return user;
            }
            return em.merge(user);
        });
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public List<User> findAll() {
        return JpaUtil.doInTransaction(em -> em.createQuery("SELECT u FROM User u", User.class).getResultList());
    }
}
