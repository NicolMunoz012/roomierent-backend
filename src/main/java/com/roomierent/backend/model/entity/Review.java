package com.roomierent.backend.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity Pattern: Representa una reseña en el dominio del negocio
 * Value Object Pattern: Rating (1-5) es un value object inmutable
 */
@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_property_created", columnList = "property_id, created_at"),
        @Index(name = "idx_user_property", columnList = "user_id, property_id", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating; // Value Object: 1-5 estrellas

    @Column(length = 1000)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        validateRating();
    }

    @PreUpdate
    protected void onUpdate() {
        validateRating();
    }

    /**
     * Template Method Pattern: Validación de rating
     */
    private void validateRating() {
        if (rating == null || rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating debe estar entre 1 y 5");
        }
    }
}