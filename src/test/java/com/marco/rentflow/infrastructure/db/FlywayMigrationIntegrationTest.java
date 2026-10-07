package com.marco.rentflow.infrastructure.db;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayMigrationIntegrationTest {

    @Test
    @DisplayName("Debe aplicar el script V1 exitosamente contra una BD PostgreSQL real")
    void flywayMigrations_ShouldApplyCleanly_AgainstRealPostgres() {
        String url;
        String user;
        String password;

        PostgreSQLContainer<?> postgres = null;
        try {
            postgres = new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test")
                    .withStartupTimeout(Duration.ofSeconds(60));
            postgres.start();
            url = postgres.getJdbcUrl();
            user = postgres.getUsername();
            password = postgres.getPassword();
        } catch (Exception e) {
            System.err.println("Testcontainers no disponible, usando PostgreSQL local en 5433 para test Flyway: " + e.getMessage());
            url = "jdbc:postgresql://localhost:5433/rentflow_core_db";
            user = "rentflow_user";
            password = "rentflow_password";
        }

        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(url, user, password)
                    .load();

            flyway.migrate();

            var migrationInfo = flyway.info().current();

            assertThat(migrationInfo).isNotNull();
            assertThat(migrationInfo.getState().isApplied()).isTrue();
            assertThat(migrationInfo.getVersion().getVersion()).isEqualTo("3");
            assertThat(migrationInfo.getDescription()).isEqualTo("add legal data to tenant invitations");
        } finally {
            if (postgres != null && postgres.isRunning()) {
                postgres.stop();
            }
        }
    }
}