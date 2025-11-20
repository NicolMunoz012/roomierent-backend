package com.roomierent.backend.controller;

import com.roomierent.backend.dto.PropertyResponse;
import com.roomierent.backend.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(
        origins = {
                "http://localhost:3000",
                "https://roomierent-frontend.vercel.app",
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
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping("/ids")
    public ResponseEntity<List<Long>> getFavoriteIds(Authentication authentication) {
        String email = authentication.getName();
        System.out.println("📥 Obteniendo IDs de favoritos para: " + email);
        return ResponseEntity.ok(favoriteService.getFavoriteIds(email));
    }

    @GetMapping
    public ResponseEntity<List<PropertyResponse>> getFavorites(Authentication authentication) {
        String email = authentication.getName();
        System.out.println("📥 Obteniendo favoritos para: " + email);
        return ResponseEntity.ok(favoriteService.getFavorites(email));
    }

    @GetMapping("/status/{propertyId}")
    public ResponseEntity<Map<String, Boolean>> isFavorite(
            Authentication authentication,
            @PathVariable Long propertyId
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(favoriteService.isFavorite(email, propertyId));
    }

    @PostMapping("/{propertyId}")
    public ResponseEntity<Map<String, Integer>> addFavorite(
            Authentication authentication,
            @PathVariable Long propertyId
    ) {
        String email = authentication.getName();
        System.out.println("📥 Agregando favorito " + propertyId + " para: " + email);
        return ResponseEntity.ok(favoriteService.addFavorite(email, propertyId));
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Map<String, Integer>> removeFavorite(
            Authentication authentication,
            @PathVariable Long propertyId
    ) {
        String email = authentication.getName();
        System.out.println("📥 Eliminando favorito " + propertyId + " para: " + email);
        return ResponseEntity.ok(favoriteService.removeFavorite(email, propertyId));
    }
}