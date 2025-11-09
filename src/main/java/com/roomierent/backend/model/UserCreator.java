package com.roomierent.backend.model;

import com.roomierent.backend.dto.SignupRequest;
import com.roomierent.backend.model.entity.User;
import com.roomierent.backend.model.entity.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserCreator {

    private final PasswordEncoder passwordEncoder;

    public UserCreator(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(SignupRequest request) {
        return User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .build();
    }


    public User createTenant(String name, String email, String password) {
        return User.builder()
                .name(name)
                .email(email.toLowerCase())
                .password(passwordEncoder.encode(password))
                .role(UserRole.TENANT)
                .active(true)
                .build();
    }

    public User createLandlord(String name, String email, String password) {
        return User.builder()
                .name(name)
                .email(email.toLowerCase())
                .password(passwordEncoder.encode(password))
                .role(UserRole.LANDLORD)
                .active(true)
                .build();
    }

    public User createInactiveUser(String name, String email, String password, UserRole role) {
        return User.builder()
                .name(name)
                .email(email.toLowerCase())
                .password(passwordEncoder.encode(password))
                .role(role)
                .active(false)
                .build();
    }
}