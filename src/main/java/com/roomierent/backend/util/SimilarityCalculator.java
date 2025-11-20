package com.roomierent.backend.util;

import com.roomierent.backend.model.entity.Property;

import java.math.BigDecimal;
import java.util.*;

/**
 * Utilidades para calcular similitud entre propiedades
 * Implementa algoritmos de Machine Learning básicos
 */
public class SimilarityCalculator {

    /**
     * Calcula la similitud de coseno entre dos propiedades
     * Retorna un valor entre 0 (no similares) y 1 (idénticas)
     */
    public static double calculateCosineSimilarity(Property p1, Property p2) {
        // Crear vectores de características
        double[] vector1 = createFeatureVector(p1);
        double[] vector2 = createFeatureVector(p2);

        // Calcular similitud de coseno
        return cosineSimilarity(vector1, vector2);
    }

    /**
     * Crea un vector de características normalizadas para una propiedad
     */
    private static double[] createFeatureVector(Property property) {
        double[] vector = new double[7];

        // Feature 0: Precio normalizado (0-1)
        vector[0] = normalizePrice(property.getPrice());

        // Feature 1: Número de habitaciones normalizado
        vector[1] = normalizeValue(property.getBedrooms(), 1, 10);

        // Feature 2: Número de baños normalizado
        vector[2] = normalizeValue(property.getBathrooms(), 1, 5);

        // Feature 3: Área normalizada
        vector[3] = normalizeValue(property.getArea(), 20, 500);

        // Feature 4: Tipo de propiedad (one-hot encoding simplificado)
        vector[4] = property.getType().ordinal() / 3.0; // 0, 0.33, 0.66, 1.0

        // Feature 5: Número de amenities normalizado
        vector[5] = normalizeValue(countAmenities(property), 0, 15);

        // Feature 6: Factor de ubicación (misma ciudad = 1, diferente = 0)
        vector[6] = 1.0; // Se ajusta en el cálculo de similitud

        return vector;
    }

    /**
     * Calcula similitud de coseno entre dos vectores
     */
    private static double cosineSimilarity(double[] v1, double[] v2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Normaliza el precio a un rango 0-1
     */
    private static double normalizePrice(BigDecimal price) {
        if (price == null) return 0.5;

        double priceValue = price.doubleValue();
        double minPrice = 100000;  // $100k
        double maxPrice = 10000000; // $10M

        return normalizeValue(priceValue, minPrice, maxPrice);
    }

    /**
     * Normaliza un valor al rango 0-1
     */
    private static double normalizeValue(double value, double min, double max) {
        if (value < min) return 0.0;
        if (value > max) return 1.0;
        return (value - min) / (max - min);
    }

    /**
     * Cuenta el número de amenities de una propiedad
     */
    private static int countAmenities(Property property) {
        if (property.getAmenities() == null || property.getAmenities().isEmpty()) {
            return 0;
        }
        return property.getAmenities().split(",").length;
    }

    /**
     * Calcula similitud considerando ubicación
     */
    public static double calculateLocationSimilarity(Property p1, Property p2) {
        double similarity = 0.0;

        // Misma ciudad: +0.5
        if (p1.getCity() != null && p1.getCity().equalsIgnoreCase(p2.getCity())) {
            similarity += 0.5;
        }

        // Mismo barrio: +0.5
        if (p1.getNeighborhood() != null &&
                p1.getNeighborhood().equalsIgnoreCase(p2.getNeighborhood())) {
            similarity += 0.5;
        }

        // Si tienen coordenadas, calcular distancia euclidiana
        if (p1.getLatitude() != null && p1.getLongitude() != null &&
                p2.getLatitude() != null && p2.getLongitude() != null) {

            double distance = calculateDistance(
                    p1.getLatitude(), p1.getLongitude(),
                    p2.getLatitude(), p2.getLongitude()
            );

            // Distancia < 1km = muy cercanas
            if (distance < 1.0) {
                similarity = Math.max(similarity, 0.9);
            } else if (distance < 5.0) {
                similarity = Math.max(similarity, 0.6);
            }
        }

        return Math.min(similarity, 1.0);
    }

    /**
     * Calcula distancia en kilómetros entre dos coordenadas
     * Usa la fórmula de Haversine
     */
    private static double calculateDistance(
            Double lat1, Double lon1,
            Double lat2, Double lon2
    ) {
        final int EARTH_RADIUS = 6371; // Radio de la Tierra en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    /**
     * Calcula similitud de amenities usando Jaccard Similarity
     */
    public static double calculateAmenitiesSimilarity(Property p1, Property p2) {
        Set<String> amenities1 = getAmenitiesSet(p1);
        Set<String> amenities2 = getAmenitiesSet(p2);

        if (amenities1.isEmpty() && amenities2.isEmpty()) {
            return 1.0; // Ambas sin amenities = similares
        }

        if (amenities1.isEmpty() || amenities2.isEmpty()) {
            return 0.0;
        }

        // Jaccard Similarity: |A ∩ B| / |A ∪ B|
        Set<String> intersection = new HashSet<>(amenities1);
        intersection.retainAll(amenities2);

        Set<String> union = new HashSet<>(amenities1);
        union.addAll(amenities2);

        return (double) intersection.size() / union.size();
    }

    /**
     * Convierte amenities de string a Set
     */
    private static Set<String> getAmenitiesSet(Property property) {
        Set<String> amenities = new HashSet<>();

        if (property.getAmenities() != null && !property.getAmenities().isEmpty()) {
            String[] parts = property.getAmenities().split(",");
            for (String amenity : parts) {
                amenities.add(amenity.trim().toLowerCase());
            }
        }

        return amenities;
    }
}