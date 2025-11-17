package com.roomierent.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    @Value("${resend.api.key}")
    private String apiKey;

    private static final String RESEND_API_URL = "https://api.resend.com/emails";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Envía email de recuperación de contraseña usando Resend
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String htmlContent = buildPasswordResetEmailBody(token);

            // Crear payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("from", "RoomieRent <onboarding@resend.dev>");
            payload.put("to", List.of(toEmail));
            payload.put("subject", "Recuperación de Contraseña - RoomieRent");
            payload.put("html", htmlContent);

            // Crear headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // Crear request
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // Enviar
            ResponseEntity<String> response = restTemplate.postForEntity(
                    RESEND_API_URL,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Email de recuperación enviado exitosamente a: {}", toEmail);
            } else {
                log.error("❌ Error enviando email a {}: {}", toEmail, response.getStatusCode());
                throw new RuntimeException("Error al enviar email: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ Error enviando email a {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Error al enviar email de recuperación", e);
        }
    }

    /**
     * Envía email de bienvenida (opcional)
     */
    public void sendWelcomeEmail(String toEmail, String name) {
        try {
            String htmlContent = buildWelcomeEmailBody(name);

            // Crear payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("from", "RoomieRent <onboarding@resend.dev>");
            payload.put("to", List.of(toEmail));
            payload.put("subject", "¡Bienvenido a RoomieRent!");
            payload.put("html", htmlContent);

            // Crear headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // Crear request
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // Enviar
            ResponseEntity<String> response = restTemplate.postForEntity(
                    RESEND_API_URL,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Email de bienvenida enviado a: {}", toEmail);
            } else {
                log.warn("⚠️ No se pudo enviar email de bienvenida a {}: {}", toEmail, response.getStatusCode());
            }

        } catch (Exception e) {
            log.warn("⚠️ Error enviando email de bienvenida a {}: {}", toEmail, e.getMessage());
            // No lanzar excepción para no bloquear el registro
        }
    }

    /**
     * Construye el HTML del email de recuperación
     */
    private String buildPasswordResetEmailBody(String token) {
        return String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #2563eb;">Recuperación de Contraseña</h2>
                <p>Hola,</p>
                <p>Recibimos una solicitud para restablecer tu contraseña en <strong>RoomieRent</strong>.</p>
                
                <div style="background-color: #f3f4f6; padding: 20px; border-radius: 8px; margin: 20px 0;">
                    <p style="margin: 0; font-size: 14px; color: #6b7280;">Tu código de recuperación es:</p>
                    <p style="font-size: 32px; font-weight: bold; color: #2563eb; margin: 10px 0; letter-spacing: 2px;">%s</p>
                </div>
                
                <p style="color: #ef4444; font-size: 14px;">⚠️ Este código expira en 15 minutos.</p>
                
                <p>Si no solicitaste este cambio, puedes ignorar este mensaje.</p>
                
                <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 30px 0;">
                <p style="font-size: 12px; color: #9ca3af;">
                    Saludos,<br>
                    <strong>Equipo RoomieRent</strong>
                </p>
            </div>
            """, token);
    }

    /**
     * Construye el HTML del email de bienvenida
     */
    private String buildWelcomeEmailBody(String name) {
        return String.format("""
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2 style="color: #2563eb;">¡Bienvenido a RoomieRent!</h2>
                <p>Hola <strong>%s</strong>,</p>
                <p>¡Gracias por registrarte en RoomieRent!</p>
                <p>Ahora puedes comenzar a explorar propiedades y encontrar tu hogar ideal.</p>
                
                <div style="margin: 30px 0;">
                    <a href="https://roomierent-frontend.vercel.app/properties" 
                       style="background-color: #2563eb; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; display: inline-block;">
                        Explorar Propiedades
                    </a>
                </div>
                
                <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 30px 0;">
                <p style="font-size: 12px; color: #9ca3af;">
                    Saludos,<br>
                    <strong>Equipo RoomieRent</strong>
                </p>
            </div>
            """, name);
    }
}