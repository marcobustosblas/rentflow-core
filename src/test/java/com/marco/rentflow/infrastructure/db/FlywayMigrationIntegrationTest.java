package com.marco.rentflow.infrastructure.db;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class FlywayMigrationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withStartupTimeout(Duration.ofSeconds(120));

    @Test
    @DisplayName("Debe aplicar el script V1 exitosamente contra una BD PostgreSQL real")
    void flywayMigrations_ShouldApplyCleanly_AgainstRealPostgres() {
        Flyway flyway = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .load();

        flyway.migrate();

        var migrationInfo = flyway.info().current();

        assertThat(migrationInfo).isNotNull();
        assertThat(migrationInfo.getState().isApplied()).isTrue();
        assertThat(migrationInfo.getVersion().getVersion()).isEqualTo("1");
        assertThat(migrationInfo.getDescription()).isEqualTo("create core tables");
    }
}