package com.roomierent.backend.service;

import com.roomierent.backend.dto.UserDTO;
import com.roomierent.backend.model.entity.User;
import com.roomierent.backend.repository.PasswordResetTokenRepository;
import com.roomierent.backend.repository.PropertyRepository;
import com.roomierent.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PropertyRepository propertyRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Busca un usuario por email
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
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
     * Elimina un usuario y todas sus dependencias en orden:
     * 1. Password reset tokens
     * 2. Propiedades del usuario
     * 3. El usuario
     */
    @Transactional
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        System.out.println("🗑️ Eliminando usuario: " + email);
        System.out.println("   - ID: " + user.getId());
        System.out.println("   - Nombre: " + user.getName());

        // 1. PRIMERO: Eliminar tokens de reseteo de contraseña
        try {
            passwordResetTokenRepository.deleteByUserId(user.getId());
            System.out.println("   ✅ Tokens de reseteo eliminados");
        } catch (Exception e) {
            System.out.println("   ⚠️ No hay tokens de reseteo para eliminar");
        }

        // 2. SEGUNDO: Eliminar propiedades del usuario
        long propertyCount = propertyRepository.countByOwnerId(user.getId());
        System.out.println("   - Propiedades a eliminar: " + propertyCount);

        if (propertyCount > 0) {
            propertyRepository.deleteByOwnerId(user.getId());
            System.out.println("   ✅ Propiedades eliminadas");
        }

        // 3. FINALMENTE: Eliminar el usuario
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