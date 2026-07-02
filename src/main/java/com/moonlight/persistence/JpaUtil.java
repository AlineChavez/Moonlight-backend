package com.moonlight.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Builds the single EntityManagerFactory for the app from environment variables and offers
 * a small transaction helper, since there's no Jakarta EE container here to manage that for us
 * (this runs on plain Tomcat).
 *
 * Connection is resolved from, in order:
 *  1. DATABASE_URL - the form Railway's Postgres plugin injects: postgresql://user:pass@host:port/db
 *  2. DB_HOST / DB_PORT / DB_NAME / DB_USER / DB_PASSWORD - individual pieces
 *  3. persistence.xml defaults (localhost, for local development only)
 */
public final class JpaUtil {

    private static final Logger LOGGER = Logger.getLogger(JpaUtil.class.getName());
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = buildFactory();

    private JpaUtil() {}

    public static EntityManagerFactory getEntityManagerFactory() {
        return ENTITY_MANAGER_FACTORY;
    }

    public static <T> T doInTransaction(Function<EntityManager, T> work) {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
        try {
            em.getTransaction().begin();
            T result = work.apply(em);
            em.getTransaction().commit();
            return result;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private static EntityManagerFactory buildFactory() {
        Map<String, Object> overrides = new HashMap<>();

        String databaseUrl = System.getenv("DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.isBlank()) {
            applyDatabaseUrl(overrides, databaseUrl);
        } else {
            String host = System.getenv().getOrDefault("DB_HOST", "localhost");
            String port = System.getenv().getOrDefault("DB_PORT", "5432");
            String name = System.getenv().getOrDefault("DB_NAME", "moonlight");
            String user = System.getenv("DB_USER");
            String password = System.getenv("DB_PASSWORD");

            overrides.put("jakarta.persistence.jdbc.url", "jdbc:postgresql://" + host + ":" + port + "/" + name);
            if (user != null) overrides.put("jakarta.persistence.jdbc.user", user);
            if (password != null) overrides.put("jakarta.persistence.jdbc.password", password);

            if (user == null || password == null) {
                LOGGER.warning("DATABASE_URL / DB_USER / DB_PASSWORD no configurados: usando los valores " +
                        "por defecto de persistence.xml (solo válido para desarrollo local).");
            }
        }

        return Persistence.createEntityManagerFactory("moonlight", overrides);
    }

    private static void applyDatabaseUrl(Map<String, Object> overrides, String databaseUrl) {
        try {
            // Java's URI can't parse "postgresql://" with a userinfo containing arbitrary
            // characters reliably in all JDKs, so normalize to a scheme URI can handle first.
            URI uri = new URI(databaseUrl.replaceFirst("^postgres(ql)?://", "scheme://"));
            String[] userInfo = uri.getUserInfo() != null ? uri.getUserInfo().split(":", 2) : new String[0];

            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + uri.getPort() + uri.getPath();
            overrides.put("jakarta.persistence.jdbc.url", jdbcUrl);
            if (userInfo.length > 0) overrides.put("jakarta.persistence.jdbc.user", userInfo[0]);
            if (userInfo.length > 1) overrides.put("jakarta.persistence.jdbc.password", userInfo[1]);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("DATABASE_URL con formato inválido", e);
        }
    }
}
