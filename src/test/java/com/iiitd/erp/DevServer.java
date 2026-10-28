package com.iiitd.erp;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.SpringApplication;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Runs the backend against an embedded PostgreSQL so no database install is needed:
 *
 *   ./mvnw spring-boot:test-run -Dspring-boot.run.main-class=com.iiitd.erp.DevServer
 *
 * Data lives in target/dev-postgres and survives restarts; delete that folder (or run
 * ./mvnw clean) to start again from the seed data.
 *
 * Environment overrides (used by the browser tests to get an isolated, fresh instance):
 *   ERP_DB_DIR   data folder (default target/dev-postgres)
 *   ERP_DB_PORT  PostgreSQL port (default 54329)
 *   ERP_FRESH    "true" wipes the data folder first
 *   SERVER_PORT  HTTP port (default 8080)
 */
public class DevServer {

    public static void main(String[] args) throws Exception {
        // DevTools' restart would call main() again and try to start a second database on the same folder.
        System.setProperty("spring.devtools.restart.enabled", "false");
        Path dataDir = Path.of(env("ERP_DB_DIR", "target/dev-postgres"));
        stopLeftoverPostgres(dataDir);
        if (Boolean.parseBoolean(env("ERP_FRESH", "false"))) {
            FileSystemUtils.deleteRecursively(dataDir);
        }
        EmbeddedPostgres postgres = EmbeddedPostgres.builder()
                .setDataDirectory(dataDir)
                .setCleanDataDirectory(false)
                .setPort(Integer.parseInt(env("ERP_DB_PORT", "54329")))
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

    /**
     * If a previous run was killed without its shutdown hook (e.g. Ctrl+C on Maven, a test runner
     * stopping it), its postgres keeps the port and data folder locked. postmaster.pid names it.
     */
    private static void stopLeftoverPostgres(Path dataDir) throws Exception {
        Path pidFile = dataDir.resolve("postmaster.pid");
        if (!Files.exists(pidFile)) {
            return;
        }
        String firstLine = Files.readAllLines(pidFile).stream().findFirst().orElse("").trim();
        if (!firstLine.matches("\\d+")) {
            return;
        }
        Optional<ProcessHandle> leftover = ProcessHandle.of(Long.parseLong(firstLine))
                .filter(p -> p.info().command().map(c -> c.endsWith("postgres")).orElse(false));
        if (leftover.isPresent()) {
            System.out.println("Stopping leftover PostgreSQL (pid " + firstLine + ") from a previous run");
            leftover.get().destroy();
            leftover.get().onExit().get(15, TimeUnit.SECONDS);
        }
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }
}
