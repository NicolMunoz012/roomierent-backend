package com.roomierent.backend.controller;

import com.roomierent.backend.dto.PropertyRequest;
import com.roomierent.backend.dto.PropertyResponse;
import com.roomierent.backend.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
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
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping
    public ResponseEntity<PropertyResponse> createProperty(
            @Valid @RequestBody PropertyRequest request
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // ✅ EMAIL real del usuario autenticado

        System.out.println("📥 Creando propiedad para: " + email);

        PropertyResponse response = propertyService.createProperty(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PropertyResponse>> getAllProperties() {
        return ResponseEntity.ok(propertyService.getAllAvailableProperties());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getPropertyById(id));
    }

    @GetMapping("/my-properties")
    public ResponseEntity<List<PropertyResponse>> getMyProperties() {
        System.out.println("========================================");
        System.out.println("🎯🎯🎯 CONTROLLER /my-properties EJECUTADO");
        System.out.println("========================================");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("🔍 Authentication object: " + auth);

        if (auth == null) {
            System.out.println("❌❌❌ Authentication es NULL");
            return ResponseEntity.status(401).build();
        }

        System.out.println("🔍 Authentication class: " + auth.getClass().getName());
        System.out.println("🔍 Principal class: " + auth.getPrincipal().getClass().getName());
        System.out.println("🔍 Principal: " + auth.getPrincipal());

        String email;
        try {
            email = auth.getName();
            System.out.println("🔍 Email extraído: " + email);
        } catch (Exception e) {
            System.out.println("❌ Error extrayendo email: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }

        try {
            List<PropertyResponse> properties = propertyService.getPropertiesByOwner(email);
            System.out.println("✅ Propiedades obtenidas: " + properties.size());
            System.out.println("========================================");
            return ResponseEntity.ok(properties);
        } catch (Exception e) {
            System.out.println("❌❌❌ Error en PropertyService: " + e.getMessage());
            e.printStackTrace();
            System.out.println("========================================");
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        propertyService.deleteProperty(id, email);
        return ResponseEntity.noContent().build();
    }
}
