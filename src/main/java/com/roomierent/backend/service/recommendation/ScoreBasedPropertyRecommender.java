package com.roomierent.backend.service.recommendation;

import com.roomierent.backend.dto.RecommendationScore;
import com.roomierent.backend.model.entity.Property;
import com.roomierent.backend.model.entity.UserPreferences;
import com.roomierent.backend.util.SimilarityCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación de recomendaciones basadas en scoring ponderado
 * Usa Machine Learning básico: scoring multi-criterio con pesos personalizados
 */
@Component
public class ScoreBasedPropertyRecommender implements PropertyRecommender {

    @Override
    public List<Property> recommend(
            List<Property> availableProperties,
            UserPreferences preferences,
            int limit
    ) {
        System.out.println("🤖 Iniciando recomendación basada en IA...");
        System.out.println("   📊 Propiedades a evaluar: " + availableProperties.size());

        // 1. Filtrar propiedades que NO cumplen requisitos mínimos
        List<Property> filteredProperties = filterByHardConstraints(
                availableProperties,
                preferences
        );

        System.out.println("   ✅ Propiedades después de filtros duros: " + filteredProperties.size());

        if (filteredProperties.isEmpty()) {
            System.out.println("   ⚠️ Ninguna propiedad cumple los requisitos mínimos");
            return new ArrayList<>();
        }

        // 2. Calcular scores para cada propiedad
        List<RecommendationScore> scoredProperties = new ArrayList<>();

        for (Property property : filteredProperties) {
            RecommendationScore score = calculateScore(property, preferences);
            scoredProperties.add(score);
        }

        // 3. Ordenar por score total (mayor a menor)
        scoredProperties.sort((a, b) ->
                Double.compare(b.getTotalScore(), a.getTotalScore())
        );

        // 4. Log de las mejores recomendaciones
        System.out.println("   🏆 Top 3 propiedades recomendadas:");
        for (int i = 0; i < Math.min(3, scoredProperties.size()); i++) {
            RecommendationScore rs = scoredProperties.get(i);
            System.out.printf("      #%d: %s (Score: %.3f)%n",
                    i + 1,
                    rs.getProperty().getTitle(),
                    rs.getTotalScore()
            );
        }

        // 5. Retornar las mejores N propiedades
        return scoredProperties.stream()
                .limit(limit)
                .map(RecommendationScore::getProperty)
                .collect(Collectors.toList());
    }

    /**
     * Filtra propiedades por restricciones duras (hard constraints)
     */
    private List<Property> filterByHardConstraints(
            List<Property> properties,
            UserPreferences preferences
    ) {
        return properties.stream()
                .filter(p -> matchesHardConstraints(p, preferences))
                .collect(Collectors.toList());
    }

    /**
     * Verifica si una propiedad cumple las restricciones mínimas
     */
    private boolean matchesHardConstraints(Property property, UserPreferences preferences) {
        // Filtro 1: Rango de precio
        if (preferences.getMinPrice() != null &&
                property.getPrice().compareTo(preferences.getMinPrice()) < 0) {
            return false;
        }

        if (preferences.getMaxPrice() != null &&
                property.getPrice().compareTo(preferences.getMaxPrice()) > 0) {
            return false;
        }

        // Filtro 2: Habitaciones mínimas
        if (preferences.getMinBedrooms() != null &&
                property.getBedrooms() < preferences.getMinBedrooms()) {
            return false;
        }

        // Filtro 3: Baños mínimos
        if (preferences.getMinBathrooms() != null &&
                property.getBathrooms() < preferences.getMinBathrooms()) {
            return false;
        }

        // Filtro 4: Área mínima
        if (preferences.getMinArea() != null &&
                (property.getArea() == null || property.getArea() < preferences.getMinArea())) {
            return false;
        }

        // Filtro 5: Ciudad preferida
        if (preferences.getPreferredCity() != null &&
                !preferences.getPreferredCity().isEmpty() &&
                !property.getCity().equalsIgnoreCase(preferences.getPreferredCity())) {
            return false;
        }

        return true;
    }

    private RecommendationScore calculateScore(
            Property property,
            UserPreferences preferences
    ) {
        RecommendationScore score = new RecommendationScore();
        score.setProperty(property);

        // Score 1: Precio (qué tan cerca está del rango ideal)
        score.setPriceScore(calculatePriceScore(property, preferences));

        // Score 2: Ubicación (ciudad, barrio)
        score.setLocationScore(calculateLocationScore(property, preferences));

        // Score 3: Amenities (servicios deseados)
        score.setAmenitiesScore(calculateAmenitiesScore(property, preferences));

        // Score 4: Tamaño (área)
        score.setSizeScore(calculateSizeScore(property, preferences));

        // Score 5: Tipo de propiedad
        score.setTypeScore(calculateTypeScore(property, preferences));

        // Calcular score total ponderado usando los pesos del usuario
        score.calculateTotalScore(
                preferences.getPriceWeight(),
                preferences.getLocationWeight(),
                preferences.getAmenitiesWeight(),
                preferences.getSizeWeight(),
                preferences.getTypeWeight()
        );

        return score;
    }

    /**
     * Score de precio: usa función Gaussiana para premiar precios cercanos al ideal
     */
    private double calculatePriceScore(Property property, UserPreferences preferences) {
        BigDecimal price = property.getPrice();
        BigDecimal minPrice = preferences.getMinPrice();
        BigDecimal maxPrice = preferences.getMaxPrice();

        // Si no hay preferencias de precio, score neutro
        if (minPrice == null || maxPrice == null) {
            return 0.5;
        }

        // Precio ideal = punto medio del rango
        BigDecimal idealPrice = minPrice.add(maxPrice).divide(BigDecimal.valueOf(2));
        BigDecimal priceRange = maxPrice.subtract(minPrice);

        // Distancia normalizada del precio al ideal
        double normalizedDistance = price.subtract(idealPrice)
                .abs()
                .divide(priceRange, BigDecimal.ROUND_HALF_UP)
                .doubleValue();

        // Función Gaussiana: máximo score cuando está en el precio ideal
        // e^(-(x^2) / 2σ^2), donde σ = 0.5
        double sigma = 0.5;
        return Math.exp(-(normalizedDistance * normalizedDistance) / (2 * sigma * sigma));
    }

    /**
     * Score de ubicación: usa matching de ciudad y barrio
     */
    private double calculateLocationScore(Property property, UserPreferences preferences) {
        double score = 0.0;

        // Ciudad preferida
        if (preferences.getPreferredCity() != null &&
                !preferences.getPreferredCity().isEmpty()) {

            if (property.getCity().equalsIgnoreCase(preferences.getPreferredCity())) {
                score += 0.6; // 60% del score por ciudad correcta
            }
        } else {
            score += 0.3; // Sin preferencia de ciudad = score neutral
        }

        // Barrios preferidos
        if (preferences.getPreferredNeighborhoods() != null &&
                !preferences.getPreferredNeighborhoods().isEmpty()) {

            String[] preferredNeighborhoods = preferences.getPreferredNeighborhoods().split(",");

            for (String neighborhood : preferredNeighborhoods) {
                if (property.getNeighborhood() != null &&
                        property.getNeighborhood().equalsIgnoreCase(neighborhood.trim())) {
                    score += 0.4; // 40% del score por barrio preferido
                    break;
                }
            }
        } else {
            score += 0.2; // Sin preferencia de barrio = score neutral
        }

        return Math.min(score, 1.0);
    }

    /**
     * Score de amenities: Jaccard Similarity entre amenities deseados y disponibles
     */
    private double calculateAmenitiesScore(Property property, UserPreferences preferences) {
        if (preferences.getDesiredAmenities() == null ||
                preferences.getDesiredAmenities().isEmpty()) {
            return 0.5; // Sin preferencias = score neutro
        }

        Set<String> desiredAmenities = new HashSet<>(
                Arrays.asList(preferences.getDesiredAmenities().split(","))
        );

        Set<String> propertyAmenities = new HashSet<>();
        if (property.getAmenities() != null && !property.getAmenities().isEmpty()) {
            propertyAmenities.addAll(
                    Arrays.asList(property.getAmenities().split(","))
            );
        }

        if (desiredAmenities.isEmpty()) {
            return 0.5;
        }

        // Normalizar a minúsculas
        desiredAmenities = desiredAmenities.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        propertyAmenities = propertyAmenities.stream()
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // Jaccard Similarity
        Set<String> intersection = new HashSet<>(desiredAmenities);
        intersection.retainAll(propertyAmenities);

        Set<String> union = new HashSet<>(desiredAmenities);
        union.addAll(propertyAmenities);

        return union.isEmpty() ? 0.5 : (double) intersection.size() / union.size();
    }

    /**
     * Score de tamaño: premia propiedades con área >= área mínima
     */
    private double calculateSizeScore(Property property, UserPreferences preferences) {
        if (preferences.getMinArea() == null || property.getArea() == null) {
            return 0.5;
        }

        double area = property.getArea();
        double minArea = preferences.getMinArea();

        if (area < minArea) {
            return 0.0; // No cumple el mínimo
        }

        // Score = 1.0 si tiene exactamente el área mínima
        // Score > 1.0 si tiene más (limitado a 1.0)
        // Bonus de hasta 20% por área extra
        double bonus = Math.min((area - minArea) / minArea, 0.2);
        return Math.min(1.0 + bonus, 1.0);
    }

    /**
     * Score de tipo: match exacto o relacionado
     */
    private double calculateTypeScore(Property property, UserPreferences preferences) {
        if (preferences.getPreferredType() == null) {
            return 0.5;
        }

        // Match exacto
        if (property.getType() == preferences.getPreferredType()) {
            return 1.0;
        }

        // Sin match
        return 0.3;
    }

    @Override
    public String getStrategyName() {
        return "Score-Based Recommender (AI/ML)";
    }
}