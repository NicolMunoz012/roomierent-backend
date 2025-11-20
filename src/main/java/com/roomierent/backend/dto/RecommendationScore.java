package com.roomierent.backend.dto;

import com.roomierent.backend.model.entity.Property;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para almacenar el score de recomendación de una propiedad
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationScore {

    private Property property;
    private double totalScore;

    // Scores individuales por categoría
    private double priceScore;
    private double locationScore;
    private double amenitiesScore;
    private double sizeScore;
    private double typeScore;

    /**
     * Calcula el score total ponderado
     */
    public void calculateTotalScore(
            double priceWeight,
            double locationWeight,
            double amenitiesWeight,
            double sizeWeight,
            double typeWeight
    ) {
        this.totalScore =
                (priceScore * priceWeight) +
                        (locationScore * locationWeight) +
                        (amenitiesScore * amenitiesWeight) +
                        (sizeScore * sizeWeight) +
                        (typeScore * typeWeight);
    }
}