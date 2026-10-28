package com.iiitd.erp;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.SpringApplication;

import java.nio.file.Path;

/**
 * Runs the backend against an embedded PostgreSQL so no database install is needed:
 *
 *   ./mvnw spring-boot:test-run -Dspring-boot.run.main-class=com.iiitd.erp.DevServer
 *
 * Data lives in target/dev-postgres and survives restarts; delete that folder (or run
 * ./mvnw clean) to start again from the seed data.
 */
public class DevServer {

    public static void main(String[] args) throws Exception {
        // DevTools' restart would call main() again and try to start a second database on the same folder.
        System.setProperty("spring.devtools.restart.enabled", "false");
        EmbeddedPostgres postgres = EmbeddedPostgres.builder()
                .setDataDirectory(Path.of("target", "dev-postgres"))
                .setCleanDataDirectory(false)
                .setPort(54329)
                .start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                postgres.close();
            } catch (Exception ignored) {
                // shutting down anyway
            }
        }));
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl("postgres", "postgres"));
        System.setProperty("spring.datasource.username", "postgres");
        System.setProperty("spring.datasource.password", "postgres");
        SpringApplication.run(UniversityErpApplication.class, args);
    }
}
