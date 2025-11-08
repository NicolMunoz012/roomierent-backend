package com.roomierent.backend.service.factory;

import com.roomierent.backend.dto.SignupRequest;
import com.roomierent.backend.model.entity.User;
import com.roomierent.backend.model.entity.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Factory Pattern: Crea usuarios según su rol
 */
@Component
public class UserFactory {

    private final PasswordEncoder passwordEncoder;

    public UserFactory(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Crea un usuario basado en la petición de signup
     */
    public User createUser(SignupRequest request) {
        return User.builder()
                .name(request.getName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .build();
    }

    /**
     * Crea un usuario Arrendatario
     */
    public User createArrendatario(String name, String email, String password) {
        return User.builder()
                .name(name)
                .email(email.toLowerCase())
                .password(passwordEncoder.encode(password))
                .role(UserRole.ARRENDATARIO)
                .active(true)
                .build();
    }

    /**
     * Crea un usuario Propietario
     */
    public User createPropietario(String name, String email, String password) {
        return User.builder()
                .name(name)
                .email(email.toLowerCase())
                .password(passwordEncoder.encode(password))
                .role(UserRole.PROPIETARIO)
                .active(true)
                .build();
    }
}