package com.roomierent.backend.controller;

import com.roomierent.backend.dto.PropertyRequest;
import com.roomierent.backend.dto.PropertyResponse;
import com.roomierent.backend.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
@CrossOrigin(origins = "http://localhost:3000")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    /**
     * POST /api/properties
     * Crea una nueva propiedad
     */
    @PostMapping
    public ResponseEntity<PropertyResponse> createProperty(
            @Valid @RequestBody PropertyRequest request,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            // Extraer email del token (temporal, luego usaremos JWT)
            String email = extractEmailFromAuth(authHeader);

            System.out.println("📥 Creando propiedad para: " + email);

            PropertyResponse response = propertyService.createProperty(request, email);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("❌ Error creando propiedad: " + e.getMessage());
            throw new RuntimeException("Error al crear la propiedad: " + e.getMessage());
        }
    }

    /**
     * GET /api/properties
     * Obtiene todas las propiedades disponibles
     */
    @GetMapping
    public ResponseEntity<List<PropertyResponse>> getAllProperties() {
        List<PropertyResponse> properties = propertyService.getAllAvailableProperties();
        return ResponseEntity.ok(properties);
    }

    /**
     * GET /api/properties/{id}
     * Obtiene una propiedad por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable Long id) {
        PropertyResponse property = propertyService.getPropertyById(id);
        return ResponseEntity.ok(property);
    }

    /**
     * GET /api/properties/my-properties
     * Obtiene las propiedades del usuario autenticado
     */
    @GetMapping("/my-properties")
    public ResponseEntity<List<PropertyResponse>> getMyProperties(
            @RequestHeader("Authorization") String authHeader
    ) {
        String email = extractEmailFromAuth(authHeader);
        List<PropertyResponse> properties = propertyService.getPropertiesByOwner(email);
        return ResponseEntity.ok(properties);
    }

    /**
     * Extrae el email del header Authorization (temporal)
     * TODO: Implementar extracción real del JWT
     */
    private String extractEmailFromAuth(String authHeader) {
        // Por ahora, asumimos que el frontend envía el email
        // Luego lo extraeremos del token JWT
        return authHeader.replace("Bearer ", "");
    }
}
