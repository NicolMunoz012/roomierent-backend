package com.roomierent.backend.service;

import com.roomierent.backend.dto.AuthResponse;
import com.roomierent.backend.dto.LoginRequest;
import com.roomierent.backend.dto.SignupRequest;
import com.roomierent.backend.model.entity.User;
import com.roomierent.backend.model.UserCreator;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Facade Pattern: Simplifica las operaciones de autenticación
 */
@Service
public class AuthService {

    private final UserService userService;
    private final UserCreator userCreator;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserService userService,
            UserCreator userCreator,
            JwtService jwtService,
            PasswordEncoder passwordEncoder
    ) {
        this.userService = userService;
        this.userCreator = userCreator;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un nuevo usuario
     */
    public AuthResponse signup(SignupRequest request) {
        // Verificar si el email ya existe
        if (userService.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Crear usuario usando Factory
        User newUser = userCreator.createUser(request);

        // Guardar en la base de datos
        User savedUser = userService.saveUser(newUser);

        // Generar token JWT
        String token = jwtService.generateToken(new UserDetailsImpl(savedUser));

        // Construir respuesta
        return AuthResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .role(savedUser.getRole())
                .token(token)
                .message("Usuario registrado exitosamente")
                .build();
    }

    /**
     * Inicia sesión de un usuario
     */
    public AuthResponse login(LoginRequest request) {
        // Buscar usuario por email
        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Contraseña incorrecta");
        }

        // Verificar si el usuario está activo
        if (!user.getActive()) {
            throw new RuntimeException("Usuario inactivo");
        }

        // Generar token JWT
        String token = jwtService.generateToken(new UserDetailsImpl(user));

        // Construir respuesta
        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .token(token)
                .message("Login exitoso")
                .build();
    }

    /**
     * UserDetails implementation para JWT
     */
    private record UserDetailsImpl(User user) implements UserDetails {

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public boolean isEnabled() {
            return user.getActive();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return java.util.List.of(() -> "ROLE_" + user.getRole().name());
        }
    }
}