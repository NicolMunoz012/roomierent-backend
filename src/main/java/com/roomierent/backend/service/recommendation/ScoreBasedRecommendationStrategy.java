package com.roomierent.backend.service.recommendation;

import com.roomierent.backend.model.entity.Property;
import com.roomierent.backend.model.entity.UserPreferences;
import com.roomierent.backend.util.datastructures.PropertyPriorityQueue;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * PATRÓN STRATEGY - Implementación concreta
 * ESTRUCTURA DE DATOS: Priority Queue (Heap)
 *
 * Estrategia de recomendación basada en scoring ponderado
 * Calcula un score para cada propiedad basado en las preferencias del usuario
 */
@Component
public class ScoreBasedRecommendationStrategy implements RecommendationStrategy {

    private final PropertyPriorityQueue priorityQueue;

    public ScoreBasedRecommendationStrategy() {
        this.priorityQueue = new PropertyPriorityQueue();
    }

    @Override
    public List<Property> recommend(List<Property> availableProperties,
                                    UserPreferences preferences,
                                    int limit) {

        priorityQueue.clear();

        System.out.println("🤖 Calculando scores para " + availableProperties.size() + " propiedades...");

        // Calcular score para cada propiedad e insertarla en el heap
        for (Property property : availableProperties) {
            double score = calculateScore(property, preferences);
            priorityQueue.insert(property, score);

            System.out.println("   📊 " + property.getTitle() + " - Score: " + String.format("%.2f", score));
        }

        // Obtener las mejores N propiedades
        List<Property> recommendations = priorityQueue.getTopN(limit);

        System.out.println("✅ Top " + recommendations.size() + " recomendaciones generadas");

        return recommendations;
    }

    /**
     * Calcula el score de una propiedad basado en las preferencias
     * Score entre 0.0 y 1.0 (mayor = mejor match)
     */
    private double calculateScore(Property property, UserPreferences preferences) {
        double totalScore = 0.0;

        // 1. Score de precio (0.0 a 1.0)
        double priceScore = calculatePriceScore(property, preferences);
        totalScore += priceScore * preferences.getPriceWeight();

        // 2. Score de ubicación (0.0 a 1.0)
        double locationScore = calculateLocationScore(property, preferences);
        totalScore += locationScore * preferences.getLocationWeight();

        // 3. Score de amenities (0.0 a 1.0)
        double amenitiesScore = calculateAmenitiesScore(property, preferences);
        totalScore += amenitiesScore * preferences.getAmenitiesWeight();

        // 4. Score de tamaño (0.0 a 1.0)
        double sizeScore = calculateSizeScore(property, preferences);
        totalScore += sizeScore * preferences.getSizeWeight();

        // 5. Score de tipo (0.0 a 1.0)
        double typeScore = calculateTypeScore(property, preferences);
        totalScore += typeScore * preferences.getTypeWeight();

        return totalScore;
    }

    private double calculatePriceScore(Property property, UserPreferences preferences) {
        if (preferences.getMinPrice() == null || preferences.getMaxPrice() == null) {
            return 1.0; // Sin preferencia de precio
        }

        double price = property.getPrice().doubleValue();
        double minPrice = preferences.getMinPrice().doubleValue();
        double maxPrice = preferences.getMaxPrice().doubleValue();

        // Precio fuera de rango = 0
        if (price < minPrice || price > maxPrice) {
            return 0.0;
        }

        // Precio en el medio del rango = 1.0
        double midPrice = (minPrice + maxPrice) / 2;
        double distance = Math.abs(price - midPrice);
        double maxDistance = (maxPrice - minPrice) / 2;

        return 1.0 - (distance / maxDistance);
    }

    private double calculateLocationScore(Property property, UserPreferences preferences) {
        double score = 0.0;

        // Ciudad preferida
        if (preferences.getPreferredCity() != null &&
                preferences.getPreferredCity().equalsIgnoreCase(property.getCity())) {
            score += 0.6;
        }

        // Barrios preferidos
        if (preferences.getPreferredNeighborhoods() != null &&
                !preferences.getPreferredNeighborhoods().isEmpty()) {

            List<String> preferredNeighborhoods = Arrays.asList(
                    preferences.getPreferredNeighborhoods().split(",")
            );

            for (String neighborhood : preferredNeighborhoods) {
                if (property.getNeighborhood().toLowerCase()
                        .contains(neighborhood.trim().toLowerCase())) {
                    score += 0.4;
                    break;
                }
            }
        } else if (score > 0) {
            score += 0.4; // Bonus si está en la ciudad correcta
        }

        return Math.min(score, 1.0);
    }

    private double calculateAmenitiesScore(Property property, UserPreferences preferences) {
        if (preferences.getDesiredAmenities() == null ||
                preferences.getDesiredAmenities().isEmpty()) {
            return 1.0; // Sin preferencia de amenities
        }

        List<String> desiredAmenities = Arrays.asList(
                preferences.getDesiredAmenities().split(",")
        );

        if (desiredAmenities.isEmpty()) {
            return 1.0;
        }

        String propertyAmenities = property.getAmenities() != null ?
                property.getAmenities().toLowerCase() : "";

        int matches = 0;
        for (String amenity : desiredAmenities) {
            if (propertyAmenities.contains(amenity.trim().toLowerCase())) {
                matches++;
            }
        }

        return (double) matches / desiredAmenities.size();
    }

    private double calculateSizeScore(Property property, UserPreferences preferences) {
        double score = 0.0;

        // Habitaciones
        if (preferences.getMinBedrooms() != null &&
                property.getBedrooms() >= preferences.getMinBedrooms()) {
            score += 0.4;
        }

        // Baños
        if (preferences.getMinBathrooms() != null &&
                property.getBathrooms() >= preferences.getMinBathrooms()) {
            score += 0.3;
        }

        // Área
        if (preferences.getMinArea() != null &&
                property.getArea() >= preferences.getMinArea()) {
            score += 0.3;
        }

        return Math.min(score, 1.0);
    }

    private double calculateTypeScore(Property property, UserPreferences preferences) {
        if (preferences.getPreferredType() == null) {
            return 1.0; // Sin preferencia de tipo
        }

        return property.getType() == preferences.getPreferredType() ? 1.0 : 0.3;
    }

    @Override
    public String getStrategyName() {
        return "Score-Based Recommendation";
    }
}