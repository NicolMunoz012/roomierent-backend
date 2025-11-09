package com.roomierent.backend.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación uno a uno con User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Preferencias de ubicación
    @Column(name = "preferred_city")
    private String preferredCity;

    @Column(name = "preferred_neighborhoods", columnDefinition = "TEXT")
    private String preferredNeighborhoods; // JSON: ["Pandiaco", "Centro"]

    // Rango de precio
    @Column(name = "min_price")
    private BigDecimal minPrice;

    @Column(name = "max_price")
    private BigDecimal maxPrice;

    // Tipo de propiedad preferido
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_type")
    private PropertyType preferredType;

    // Características preferidas
    @Column(name = "min_bedrooms")
    @Builder.Default
    private Integer minBedrooms = 1;

    @Column(name = "min_bathrooms")
    @Builder.Default
    private Integer minBathrooms = 1;

    @Column(name = "min_area")
    private Double minArea;

    // Amenities deseados (JSON)
    @Column(name = "desired_amenities", columnDefinition = "TEXT")
    private String desiredAmenities; // JSON: ["wifi", "parking", "furnished"]

    // Pesos para el algoritmo de recomendación (0.0 a 1.0)
    @Column(name = "price_weight")
    @Builder.Default
    private Double priceWeight = 0.3; // Importancia del precio

    @Column(name = "location_weight")
    @Builder.Default
    private Double locationWeight = 0.25; // Importancia de la ubicación

    @Column(name = "amenities_weight")
    @Builder.Default
    private Double amenitiesWeight = 0.2; // Importancia de los servicios

    @Column(name = "size_weight")
    @Builder.Default
    private Double sizeWeight = 0.15; // Importancia del tamaño

    @Column(name = "type_weight")
    @Builder.Default
    private Double typeWeight = 0.1; // Importancia del tipo

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}