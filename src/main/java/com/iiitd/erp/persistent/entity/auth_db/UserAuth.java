package com.iiitd.erp.persistent.entity.auth_db;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_auth", schema = "auth_db")
public class UserAuth {
    @Id
    @Column(name = "user_id", columnDefinition = "uuid")
    private UUID userId;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    @Column(name = "password_history", columnDefinition = "jsonb")
    private String passwordHistory;

    // getters/setters
}
