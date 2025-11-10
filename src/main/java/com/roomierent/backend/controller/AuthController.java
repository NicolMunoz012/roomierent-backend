package com.roomierent.backend.controller;

import com.roomierent.backend.dto.ForgotPasswordRequest;
import com.roomierent.backend.dto.ResetPasswordRequest;
import com.roomierent.backend.service.PasswordResetService;
import com.roomierent.backend.dto.AuthResponse;
import com.roomierent.backend.dto.LoginRequest;
import com.roomierent.backend.dto.SignupRequest;
import com.roomierent.backend.service.AuthService;
import com.roomierent.backend.service.UserService;
import com.roomierent.backend.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final JwtService jwtService;

    public AuthController(AuthService authService,
                          PasswordResetService passwordResetService,
                          UserService userService,
                          JwtService jwtService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        try {
            System.out.println("📥 Recibiendo petición de registro para: " + request.getEmail());

            AuthResponse response = authService.signup(request);

            System.out.println("✅ Usuario creado exitosamente: " + response.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            System.err.println("❌ Error en registrarse: " + e.getMessage());

            if (e.getMessage().contains("El correo ya existe")) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(AuthResponse.builder()
                                .message(e.getMessage())
                                .build());
            }

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.builder()
                            .message(e.getMessage())
                            .build());
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
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
            System.out.println("📥 Recibiendo petición de Iniciar Sesión para: " + request.getEmail());

            AuthResponse response = authService.login(request);

            System.out.println("✅ Inicio de Sesión exitoso: " + response.getEmail());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error en Inicio de Sesión: " + e.getMessage());

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

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            System.out.println("📧 Solicitud de recuperación para: " + request.getEmail());

            passwordResetService.initiatePasswordReset(request);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Se ha enviado un correo con instrucciones para recuperar tu contraseña");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error en ´¿Olvidó su contraseña?´: " + e.getMessage());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Si el correo existe, recibirás instrucciones para recuperar tu contraseña");

            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            System.out.println("🔑 Reseteo de contraseña con token");

            passwordResetService.resetPassword(request);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Contraseña actualizada exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.err.println("❌ Error en restablecer contraseña: " + e.getMessage());

            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * DELETE /api/auth/delete-account
     * Elimina la cuenta del usuario autenticado
     *
     * Requiere JWT en el header: Authorization: Bearer {token}
     */
    @DeleteMapping("/delete-account")
    public ResponseEntity<Map<String, String>> deleteAccount() {
        try {
            // Obtener el usuario autenticado desde el SecurityContext
            // (ya fue poblado por JwtAuthenticationFilter)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                System.err.println("❌ Usuario no autenticado");
                Map<String, String> response = new HashMap<>();
                response.put("message", "No autenticado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String email = authentication.getName();
            System.out.println("🗑️ Solicitud de eliminación de cuenta: " + email);

            userService.deleteUserByEmail(email);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Cuenta eliminada exitosamente");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.err.println("❌ Error eliminando cuenta: " + e.getMessage());

            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            System.err.println("❌ Error interno: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Error interno del servidor");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Método alternativo usando JwtService directamente
     * (por si prefieres esta forma)
     */
    @DeleteMapping("/delete-account-alt")
    public ResponseEntity<Map<String, String>> deleteAccountAlt(
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            // Validar formato del header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Token JWT inválido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Extraer el token
            String jwt = authHeader.substring(7);

            // Extraer el email del token usando JwtService
            String email = jwtService.extractEmail(jwt);

            System.out.println("🗑️ Solicitud de eliminación de cuenta: " + email);

            userService.deleteUserByEmail(email);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Cuenta eliminada exitosamente");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error eliminando cuenta: " + e.getMessage());
            e.printStackTrace();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Error al eliminar la cuenta");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}