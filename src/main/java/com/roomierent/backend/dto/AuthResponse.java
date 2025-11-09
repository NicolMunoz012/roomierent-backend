package com.roomierent.backend.dto;

import com.roomierent.backend.model.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private Long id;
    private String email;
    private String name;
    private Role role;
    private String token;  // JWT token
    private String message;
}