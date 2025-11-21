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
@CrossOrigin(
        origins = {
                "http://localhost:3000",
                "https://roomierent-frontend.vercel.app",
                "https://roomierent-frontend-git-main-nicols-projects-baf675f0.vercel.app",
                "https://*.vercel.app",
                "https://www.roomierent.online/"
        },
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        },
        allowCredentials = "true"
)
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
            System.out.println("🔍 ROL RECIBIDO: " + request.getRole());  // ✅ AGREGAR
            System.out.println("🔍 NOMBRE RECIBIDO: " + request.getName());  // ✅ AGREGAR

            AuthResponse response = authService.signup(request);

            System.out.println("🔍 ROL EN RESPUESTA: " + response.getRole());  // ✅ AGREGAR
            System.out.println("✅ Usuario creado exitosamente: " + response.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            System.err.println("❌ Error en registrarse: " + e.getMessage());

            return ResponseEntity
                    .status(e.getMessage().contains("El correo ya existe") ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST)
                    .body(AuthResponse.builder().message(e.getMessage()).build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthResponse.builder().message("Error interno del servidor").build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            System.out.println("📥 Recibiendo petición de Iniciar Sesión para: " + request.getEmail());
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder().message("Email o contraseña incorrectos").build());
        }
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Backend funcionando correctamente! 🚀");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            passwordResetService.initiatePasswordReset(request);
            response.put("message", "Se ha enviado un correo con instrucciones para recuperar tu contraseña");
        } catch (Exception e) {
            response.put("message", "Si el correo existe, recibirás instrucciones para recuperar tu contraseña");
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            passwordResetService.resetPassword(request);
            response.put("message", "Contraseña actualizada exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/delete-account")
    public ResponseEntity<Map<String, String>> deleteAccount() {
        Map<String, String> response = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                response.put("message", "No autenticado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            String email = authentication.getName();
            userService.deleteUserByEmail(email);
            response.put("message", "Cuenta eliminada exitosamente");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/delete-account-alt")
    public ResponseEntity<Map<String, String>> deleteAccountAlt(@RequestHeader("Authorization") String authHeader) {
        Map<String, String> response = new HashMap<>();
        try {
            if (!authHeader.startsWith("Bearer ")) {
                response.put("message", "Token JWT inválido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String email = jwtService.extractEmail(authHeader.substring(7));
            userService.deleteUserByEmail(email);
            response.put("message", "Cuenta eliminada exitosamente");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("message", "Error al eliminar la cuenta");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
