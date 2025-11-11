package com.roomierent.backend.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        name = "spring.mail.username",
        matchIfMissing = false
) // ← Solo se activa si hay username configurado
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Recuperación de contraseña - RoomieRent");
            message.setText("Tu código de recuperación es: " + token +
                    "\n\nEste código expira en 1 hora.");

            mailSender.send(message);
            System.out.println("✅ Email enviado exitosamente a: " + to);

        } catch (Exception e) {
            System.err.println("❌ Error enviando email: " + e.getMessage());
            throw new RuntimeException("Error al enviar email", e);
        }
    }
}