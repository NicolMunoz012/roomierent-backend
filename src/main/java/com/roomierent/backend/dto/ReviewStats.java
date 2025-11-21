package com.roomierent.backend.dto;

import lombok.*;

/**
 * Value Object Pattern: Estadísticas inmutables de reseñas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStats {
    private Double averageRating;
    private Long totalReviews;
    private Integer[] ratingDistribution; // [1star, 2star, 3star, 4star, 5star]
}