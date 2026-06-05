package com.cryptomining.platform.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data @Builder
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private Set<String> roles;
    private String accountStatus;
    private boolean mfaEnabled;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
