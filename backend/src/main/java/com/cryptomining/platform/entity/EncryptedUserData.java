package com.cryptomining.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "encrypted_user_data")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EncryptedUserData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "data_type", nullable = false)
    private String dataType;

    @Lob
    @Column(name = "encrypted_payload", nullable = false)
    private byte[] encryptedPayload;

    @Column(nullable = false)
    private String iv;

    @Column(name = "key_salt", nullable = false)
    private String keySalt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
