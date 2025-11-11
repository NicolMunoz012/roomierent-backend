package com.roomierent.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // Injected email address for the sender
    @Value("${spring.mail.username}")
    private String fromEmail;

    // Injected URL for the frontend application
    @Value("${app.frontend.url}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends a password reset email
     *
     * @param toEmail The recipient's email address
     * @param token The password reset token
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Restablecimiento de Contraseña - RoomieRent"); // Asunto en español

        // CUERPO DEL EMAIL EN ESPAÑOL
        message.setText(
                "Hola,\n\n" +
                        "Usted ha solicitado restablecer la contraseña de su cuenta en RoomieRent.\n\n" +
                        "Haga clic en el siguiente enlace para restablecer su contraseña:\n" +
                        resetLink + "\n\n" +
                        "Este enlace expirará en 1 hora.\n\n" +
                        "Si usted no solicitó este cambio, por favor ignore este correo.\n\n" +
                        "Saludos cordiales,\n" +
                        "El equipo de RoomieRent"
        );

        mailSender.send(message);

        // Log en la consola para el desarrollador (se mantiene en español/spanglish por contexto)
        System.out.println("✅ Email de recuperación enviado a: " + toEmail);
    }
}