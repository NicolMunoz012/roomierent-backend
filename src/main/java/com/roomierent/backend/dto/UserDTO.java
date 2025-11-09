package com.roomierent.backend.dto;

import com.roomierent.backend.model.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;
    private String email;
    private String name;
    private Role role;
    private Boolean active;
    private LocalDateTime createdAt;
}