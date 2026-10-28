package com.iiitd.erp.support;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Boots the full application against a real (embedded) PostgreSQL. Every test starts
 * from a freshly migrated database containing the V2 seed data.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class PostgresIntegrationTest {

    private static final EmbeddedPostgres POSTGRES;

    static {
        try {
            POSTGRES = EmbeddedPostgres.builder().start();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
        registry.add("spring.flyway.schemas", () -> "public,auth_db,erp_db");
        registry.add("spring.flyway.clean-disabled", () -> "false");
    }

    @Autowired private Flyway flyway;
    @Autowired protected MockMvc mvc;
    @Autowired protected JdbcTemplate jdbc;

    @BeforeEach
    void resetDatabase() {
        flyway.clean();
        flyway.migrate();
    }

    protected ResultActions attemptLogin(String username, String password) throws Exception {
        return mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)));
    }

    protected MockHttpSession login(String username, String password) throws Exception {
        return (MockHttpSession) attemptLogin(username, password)
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false);
    }
}
