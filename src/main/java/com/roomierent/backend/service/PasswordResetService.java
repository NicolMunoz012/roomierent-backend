package com.roomierent.backend.service;

import com.roomierent.backend.dto.ForgotPasswordRequest;
import com.roomierent.backend.dto.ResetPasswordRequest;
import com.roomierent.backend.model.entity.PasswordResetToken;
import com.roomierent.backend.model.entity.User;
import com.roomierent.backend.repository.PasswordResetTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserService userService;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetService(
            UserService userService,
            PasswordResetTokenRepository tokenRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder
    ) {
        this.userService = userService;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Inicia el proceso de recuperación de contraseña
     */
    @Transactional
    public void initiatePasswordReset(ForgotPasswordRequest request) {
        // Buscar usuario por email
        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Eliminar tokens anteriores del usuario
        tokenRepository.deleteByUser(user);

        // Generar nuevo token
        String token = UUID.randomUUID().toString();

        // Crear token con expiración de 1 hora
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        // Enviar email
        emailService.sendPasswordResetEmail(user.getEmail(), token);

        System.out.println("🔑 Token de recuperación generado para: " + user.getEmail());
    }

    /**
     * Resetea la contraseña usando el token
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // Buscar token
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        // Validar token
        if (resetToken.isExpired()) {
            throw new RuntimeException("El token ha expirado");
        }

        if (resetToken.getUsed()) {
            throw new RuntimeException("El token ya fue utilizado");
        }

        // Actualizar contraseña del usuario
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.saveUser(user);

        // Marcar token como usado
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        System.out.println("✅ Contraseña actualizada para: " + user.getEmail());
    }
}