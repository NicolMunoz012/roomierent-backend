package com.roomierent.backend.controller;

import com.roomierent.backend.dto.PropertyResponse;
import com.roomierent.backend.dto.UserPreferencesRequest;
import com.roomierent.backend.dto.UserPreferencesResponse;
import com.roomierent.backend.model.entity.Property;
import com.roomierent.backend.service.PropertyService;
import com.roomierent.backend.service.UserPreferencesService;
import com.roomierent.backend.service.recommendation.RecommendationManager;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
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
public class RecommendationController {

    private final RecommendationManager recommendationManager;
    private final UserPreferencesService preferencesService;
    private final PropertyService propertyService;

    public RecommendationController(RecommendationManager recommendationManager,
                                    UserPreferencesService preferencesService,
                                    PropertyService propertyService) {
        this.recommendationManager = recommendationManager;
        this.preferencesService = preferencesService;
        this.propertyService = propertyService;
    }

    /**
     * GET /api/recommendations
     * Obtiene recomendaciones personalizadas para el usuario autenticado
     */
    @GetMapping
    public ResponseEntity<List<PropertyResponse>> getRecommendations(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            String email = extractEmailFromAuth(authHeader);

            System.out.println("📥 Petición de recomendaciones para: " + email);

            List<Property> recommendations = recommendationManager.getRecommendationsForUser(email, limit);

            List<PropertyResponse> response = recommendations.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error generando recomendaciones: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/recommendations/similar/{propertyId}
     * Obtiene propiedades similares a una dada
     */
    @GetMapping("/similar/{propertyId}")
    public ResponseEntity<List<PropertyResponse>> getSimilarProperties(
            @PathVariable Long propertyId,
            @RequestParam(defaultValue = "5") int limit
    ) {
        try {
            System.out.println("📥 Petición de propiedades similares a ID: " + propertyId);

            List<Property> similarProperties = recommendationManager.getSimilarProperties(propertyId, limit);

            List<PropertyResponse> response = similarProperties.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo propiedades similares: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * POST /api/recommendations/preferences
     * Guarda o actualiza las preferencias del usuario
     */
    @PostMapping("/preferences")
    public ResponseEntity<UserPreferencesResponse> savePreferences(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody UserPreferencesRequest request
    ) {
        try {
            String email = extractEmailFromAuth(authHeader);

            System.out.println("📥 Guardando preferencias para: " + email);

            UserPreferencesResponse response = preferencesService.saveOrUpdatePreferences(email, request);

            System.out.println("✅ Preferencias guardadas");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error guardando preferencias: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/recommendations/preferences
     * Obtiene las preferencias del usuario
     */
    @GetMapping("/preferences")
    public ResponseEntity<UserPreferencesResponse> getPreferences(
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String email = extractEmailFromAuth(authHeader);

            UserPreferencesResponse response = preferencesService.getPreferences(email);

            if (response == null) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error obteniendo preferencias: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * POST /api/recommendations/build-graph
     * Construye el grafo de similitud (admin/mantenimiento)
     */
    @PostMapping("/build-graph")
    public ResponseEntity<Map<String, String>> buildGraph() {
        try {
            System.out.println("📥 Petición para construir grafo de similitud");

            recommendationManager.buildPropertyGraph();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Grafo de similitud construido exitosamente");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Error construyendo grafo: " + e.getMessage());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Error construyendo grafo");

            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * GET /api/recommendations/strategies
     * Lista las estrategias de recomendación disponibles
     */
    @GetMapping("/strategies")
    public ResponseEntity<List<String>> getStrategies() {
        List<String> strategies = recommendationManager.getAvailableStrategies();
        return ResponseEntity.ok(strategies);
    }

    /**
     * Convierte Property a PropertyResponse
     * (Reutilizando la lógica de PropertyService)
     */
    private PropertyResponse convertToResponse(Property property) {
        return propertyService.convertToResponse(property);
    }

    /**
     * Extrae el email del header Authorization
     */
    private String extractEmailFromAuth(String authHeader) {
        return authHeader.replace("Bearer ", "");
    }
}