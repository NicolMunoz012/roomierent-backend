package com.roomierent.backend.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    // 🔴 Temporalmente deshabilitado para evitar error JavaMailSender

    public void sendPasswordResetEmail(String email, String token) {
        System.out.println("⚠️ Simulando envío de correo a: " + email);
        System.out.println("🔗 Token: " + token);
    }
}
