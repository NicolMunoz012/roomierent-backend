package com.roomierent.backend.validation;

import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class UserValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    );

    /**
     * Valida formato de email
     * @param email El email a validar
     * @return true si es válido
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Valida contraseña robusta:
     * - Mínimo 8 caracteres
     * - Al menos 1 mayúscula
     * - Al menos 1 minúscula
     * - Al menos 1 número
     * - Al menos 1 carácter especial
     * - Sin espacios
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Valida nombre (firstName/lastName)
     * - No vacío
     * - Entre 2 y 50 caracteres
     * - Solo letras y espacios
     */
    public boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        String trimmed = name.trim();
        return trimmed.length() >= 2 &&
                trimmed.length() <= 50 &&
                trimmed.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$");
    }

    /**
     * Valida número de teléfono
     * - Solo números
     * - Entre 7 y 15 dígitos
     */
    public boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleaned = phone.replaceAll("[\\s()-]", "");
        return cleaned.matches("^[0-9]{7,15}$");
    }

    /**
     * Sanitiza string eliminando espacios al inicio/final
     */
    public String sanitize(String input) {
        return input == null ? null : input.trim();
    }
}