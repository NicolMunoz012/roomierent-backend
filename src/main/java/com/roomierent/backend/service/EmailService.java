package com.roomierent.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Envía email de recuperación de contraseña
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Recuperación de Contraseña - RoomieRent");
            message.setText(buildPasswordResetEmailBody(token));

            mailSender.send(message);
            log.info("✅ Email de recuperación enviado a: {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Error enviando email a {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Error al enviar email de recuperación", e);
        }
    }

    /**
     * Construye el cuerpo del email
     */
    private String buildPasswordResetEmailBody(String token) {
        return String.format("""
            Hola,
            
            Recibimos una solicitud para restablecer tu contraseña en RoomieRent.
            
            Tu código de recuperación es: %s
            
            Este código expira en 15 minutos.
            
            Si no solicitaste este cambio, puedes ignorar este mensaje.
            
            Saludos,
            Equipo RoomieRent
            """, token);
    }

    /**
     * Envía email de bienvenida (opcional)
     */
    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("¡Bienvenido a RoomieRent!");
            message.setText(String.format("""
                Hola %s,
                
                ¡Gracias por registrarte en RoomieRent!
                
                Ahora puedes comenzar a explorar propiedades y encontrar tu hogar ideal.
                
                Saludos,
                Equipo RoomieRent
                """, name));

            mailSender.send(message);
            log.info("✅ Email de bienvenida enviado a: {}", toEmail);

        } catch (Exception e) {
            log.error("⚠️ Error enviando email de bienvenida a {}: {}", toEmail, e.getMessage());
            // No lanzar excepción aquí para no bloquear el registro
        }
    }
}