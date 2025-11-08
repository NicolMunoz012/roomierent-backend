package com.roomierent.backend.controller;

import com.roomierent.backend.dto.ForgotPasswordRequest;
import com.roomierent.backend.dto.ResetPasswordRequest;
import com.roomierent.backend.service.PasswordResetService;
import com.roomierent.backend.dto.AuthResponse;
import com.roomierent.backend.dto.LoginRequest;
import com.roomierent.backend.dto.SignupRequest;
import com.roomierent.backend.service.AuthService;
import com.roomierent.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final UserService userService;

    // Constructor con todas las dependencias
    public AuthController(AuthService authService,
                          PasswordResetService passwordResetService,
                          UserService userService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.userService = userService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        try {
            System.out.println("📥 Recibiendo petición de signup para: " + request.getEmail());

            AuthResponse response = authService.signup(request);

            System.out.println("✅ Usuario creado exitosamente: " + response.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            System.err.println("❌ Error en signup: " + e.getMessage());

            // Si el email ya existe
            if (e.getMessage().contains("ya está registrado")) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT) // 409 Conflict
                        .body(AuthResponse.builder()
                                .message(e.getMessage())
                                .build());
            }

            // Otros errores
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.builder()
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            System.err.println("❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder()
                            .message("Error interno del servidor")
                            .build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            System.out.println("📥 Recibiendo petición de login para: " + request.getEmail());

            AuthResponse response = authService.login(request);

            System.out.println("✅ Login exitoso: " + response.getEmail());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error en login: " + e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .message("Email o contraseña incorrectos")
                            .build());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Backend funcionando correctamente! 🚀");
    }

    /**
     * POST /api/auth/forgot-password
     * Solicita recuperación de contraseña
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            System.out.println("📧 Solicitud de recuperación para: " + request.getEmail());

            passwordResetService.initiatePasswordReset(request);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Se ha enviado un correo con instrucciones para recuperar tu contraseña");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error en forgot-password: " + e.getMessage());

            // Por seguridad, siempre devolvemos el mismo mensaje aunque el email no exista
            Map<String, String> response = new HashMap<>();
            response.put("message", "Si el correo existe, recibirás instrucciones para recuperar tu contraseña");

            return ResponseEntity.ok(response);
        }
    }

    /**
     * POST /api/auth/reset-password
     * Resetea la contraseña con el token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            System.out.println("🔑 Reseteo de contraseña con token");

            passwordResetService.resetPassword(request);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Contraseña actualizada exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.err.println("❌ Error en reset-password: " + e.getMessage());

            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * DELETE /api/auth/delete-account
     * Elimina la cuenta del usuario y todas sus propiedades
     */
    @DeleteMapping("/delete-account")
    public ResponseEntity<Map<String, String>> deleteAccount(
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String email = extractEmailFromAuth(authHeader);

            System.out.println("🗑️ Solicitud de eliminación de cuenta: " + email);

            userService.deleteUserByEmail(email);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Cuenta eliminada exitosamente");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error eliminando cuenta: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Error al eliminar la cuenta: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Extrae el email del header Authorization (temporal)
     * TODO: Implementar extracción real del JWT
     */
    private String extractEmailFromAuth(String authHeader) {
        // Por ahora, asumimos que el frontend envía el email directamente
        // Luego lo extraeremos del token JWT
        return authHeader.replace("Bearer ", "");
    }
}