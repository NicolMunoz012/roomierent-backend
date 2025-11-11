package com.roomierent.backend.service;

import com.roomierent.backend.dto.ForgotPasswordRequest;
import com.roomierent.backend.dto.ResetPasswordRequest;
import com.roomierent.backend.model.entity.PasswordResetToken;
import com.roomierent.backend.model.entity.User;
import com.roomierent.backend.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserService userService;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    // ← CLAVE: required = false (no falla si no existe)
    @Autowired(required = false)
    private EmailService emailService;

    public PasswordResetService(
            UserService userService,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userService = userService;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void initiatePasswordReset(ForgotPasswordRequest request) {
        User user = userService.findByEmail(request.getEmail());

        try {
            tokenRepository.deleteByUserId(user.getId());
        } catch (Exception e) {
            // Ignorar si no hay tokens previos
        }

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        // Enviar email SOLO si el servicio está disponible
        if (emailService != null) {
            try {
                emailService.sendPasswordResetEmail(user.getEmail(), token);
                System.out.println("📧 Email de recuperación enviado a: " + user.getEmail());
            } catch (Exception e) {
                System.err.println("⚠️ Error enviando email, pero token creado: " + token);
            }
        } else {
            System.out.println("⚠️ EmailService no configurado. Token generado: " + token);
            System.out.println("   Usuario puede usar este token manualmente.");
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (resetToken.isExpired()) {
            throw new RuntimeException("El token ha expirado");
        }

        if (resetToken.getUsed()) {
            throw new RuntimeException("El token ya fue utilizado");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.saveUser(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        System.out.println("✅ Contraseña actualizada para: " + user.getEmail());
    }
}