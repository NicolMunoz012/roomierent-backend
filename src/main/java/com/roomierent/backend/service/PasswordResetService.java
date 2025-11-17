package com.roomierent.backend.service;

import com.roomierent.backend.dto.ForgotPasswordRequest;
import com.roomierent.backend.dto.ResetPasswordRequest;
import com.roomierent.backend.model.entity.PasswordResetToken;
import com.roomierent.backend.model.entity.User;
import com.roomierent.backend.repository.PasswordResetTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class PasswordResetService {

    private final UserService userService;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetService(
            UserService userService,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.userService = userService;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void initiatePasswordReset(ForgotPasswordRequest request) {
        User user = userService.findByEmail(request.getEmail());

        // Eliminar tokens previos del usuario
        try {
            tokenRepository.deleteByUserId(user.getId());
        } catch (Exception e) {
            log.warn("No hay tokens previos para eliminar");
        }

        // Generar nuevo token
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        // Enviar email
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), token);
            log.info("✅ Email de recuperación enviado a: {}", user.getEmail());
        } catch (Exception e) {
            log.error("❌ Error enviando email a {}: {}", user.getEmail(), e.getMessage());
            // No lanzar excepción - el token ya fue creado
            log.warn("⚠️ Token creado pero email no enviado: {}", token);
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

        log.info("✅ Contraseña actualizada para: {}", user.getEmail());
    }
}