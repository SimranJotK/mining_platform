package com.cryptomining.platform.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Set;

@Data @Builder
public class UserDto {
    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private Set<String> roles;
    private boolean mfaEnabled;
}
