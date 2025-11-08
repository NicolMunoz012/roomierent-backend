package com.roomierent.backend.service;

import com.roomierent.backend.dto.UserDTO;
import com.roomierent.backend.model.entity.User;
import com.roomierent.backend.repository.UserRepository;
import com.roomierent.backend.repository.PropertyRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final PasswordEncoder passwordEncoder;

    // Constructor único con todas las dependencias
    public UserService(UserRepository userRepository,
                       PropertyRepository propertyRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Busca un usuario por email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase());
    }

    /**
     * Verifica si un email ya existe
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email.toLowerCase());
    }

    /**
     * Guarda un usuario en la base de datos
     */
    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Convierte un User a UserDTO (sin exponer la contraseña)
     */
    public UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Elimina un usuario y todas sus propiedades asociadas
     */
    @Transactional
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        System.out.println("🗑️ Eliminando usuario: " + email);
        System.out.println("   - ID: " + user.getId());
        System.out.println("   - Nombre: " + user.getName());

        // 1. Primero eliminar todas las propiedades del usuario
        long propertyCount = propertyRepository.countByOwnerId(user.getId());
        System.out.println("   - Propiedades a eliminar: " + propertyCount);

        if (propertyCount > 0) {
            propertyRepository.deleteByOwnerId(user.getId());
            System.out.println("   ✅ Propiedades eliminadas");
        }

        // 2. Luego eliminar el usuario
        userRepository.delete(user);

        System.out.println("✅ Usuario eliminado exitosamente");
    }

    /**
     * Obtiene el número de propiedades de un usuario
     */
    public long getPropertyCountByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return propertyRepository.countByOwnerId(user.getId());
    }
}