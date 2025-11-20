package com.roomierent.backend.config;

import com.roomierent.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        System.out.println("🔍 JWT Filter - Path: " + request.getRequestURI());
        System.out.println("🔍 JWT Filter - Method: " + request.getMethod());
        System.out.println("🔍 JWT Filter - Auth Header presente: " + (authHeader != null));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("⚠️ JWT Filter - No hay Bearer token, continuando sin autenticar");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            System.out.println("🔍 JWT Filter - Token extraído (primeros 30 chars): " + jwt.substring(0, Math.min(30, jwt.length())) + "...");

            final String userEmail = jwtService.extractEmail(jwt);
            System.out.println("🔍 JWT Filter - Email extraído del token: " + userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                System.out.println("🔍 JWT Filter - Cargando UserDetails para: " + userEmail);

                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                System.out.println("✅ JWT Filter - UserDetails cargado: " + userDetails.getUsername());

                boolean isValid = jwtService.isTokenValid(jwt, userDetails);
                System.out.println("🔍 JWT Filter - Token válido: " + isValid);

                if (isValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("✅ JWT Filter - Autenticación establecida para: " + userEmail);
                } else {
                    System.out.println("❌ JWT Filter - Token INVÁLIDO para: " + userEmail);
                }
            } else if (userEmail == null) {
                System.out.println("❌ JWT Filter - No se pudo extraer email del token");
            } else {
                System.out.println("⚠️ JWT Filter - Usuario ya autenticado");
            }
        } catch (Exception e) {
            System.out.println("❌❌❌ JWT Filter - EXCEPCIÓN CAPTURADA: " + e.getClass().getName());
            System.out.println("❌ Mensaje: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}