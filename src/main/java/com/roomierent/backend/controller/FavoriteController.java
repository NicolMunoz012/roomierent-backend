package com.roomierent.backend.controller;

import com.roomierent.backend.dto.PropertyResponse;
import com.roomierent.backend.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(
        origins = {
                "http://localhost:3000",
                "https://roomierent-frontend.vercel.app"
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

    // Nota: Igual que en PropertyController, de momento el email viene en Authorization: Bearer <email>
    private String extractEmailFromAuth(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new RuntimeException("Authorization header inválido");
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }
        // Soportar email directo sin prefijo Bearer (como en el frontend actual)
        return authorization.trim();
    }

    @GetMapping("/ids")
    public ResponseEntity<List<Long>> getFavoriteIds(@RequestHeader("Authorization") String authorization) {
        String email = extractEmailFromAuth(authorization);
        List<Long> ids = favoriteService.getFavoriteIds(email);
        return ResponseEntity.ok(ids);
    }

    @GetMapping
    public ResponseEntity<List<PropertyResponse>> getFavorites(@RequestHeader("Authorization") String authorization) {
        String email = extractEmailFromAuth(authorization);
        List<PropertyResponse> favorites = favoriteService.getFavorites(email);
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/status/{propertyId}")
    public ResponseEntity<Map<String, Boolean>> isFavorite(@RequestHeader("Authorization") String authorization,
                                                           @PathVariable Long propertyId) {
        String email = extractEmailFromAuth(authorization);
        Map<String, Boolean> result = favoriteService.isFavorite(email, propertyId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{propertyId}")
    public ResponseEntity<Map<String, Integer>> addFavorite(@RequestHeader("Authorization") String authorization,
                                                            @PathVariable Long propertyId) {
        String email = extractEmailFromAuth(authorization);
        Map<String, Integer> updated = favoriteService.addFavorite(email, propertyId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Map<String, Integer>> removeFavorite(@RequestHeader("Authorization") String authorization,
                                                               @PathVariable Long propertyId) {
        String email = extractEmailFromAuth(authorization);
        Map<String, Integer> updated = favoriteService.removeFavorite(email, propertyId);
        return ResponseEntity.ok(updated);
    }
}