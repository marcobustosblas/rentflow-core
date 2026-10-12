package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
public abstract class AbstractIntegrationTest {

    private static PostgreSQLContainer<?> postgres;

    static {
        try {
            PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("rentflow_test")
                    .withUsername("test")
                    .withPassword("test");
            container.start();
            postgres = container;
        } catch (Exception e) {
            System.err.println("Testcontainers no pudo iniciar el contenedor PostgreSQL. Usando base de datos PostgreSQL local en localhost:5433. Error: " + e.getMessage());
            postgres = null;
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        if (postgres != null && postgres.isRunning()) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
            registry.add("spring.flyway.url", postgres::getJdbcUrl);
            registry.add("spring.flyway.user", postgres::getUsername);
            registry.add("spring.flyway.password", postgres::getPassword);
        } else {
            registry.add("spring.datasource.url", () -> "jdbc:postgresql://localhost:5433/rentflow_core_db");
            registry.add("spring.datasource.username", () -> "rentflow_user");
            registry.add("spring.datasource.password", () -> "rentflow_password");
            registry.add("spring.flyway.url", () -> "jdbc:postgresql://localhost:5433/rentflow_core_db");
            registry.add("spring.flyway.user", () -> "rentflow_user");
            registry.add("spring.flyway.password", () -> "rentflow_password");
        }
    }
}