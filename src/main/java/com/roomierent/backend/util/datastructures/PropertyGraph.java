package com.roomierent.backend.util.datastructures;

import com.roomierent.backend.model.entity.Property;
import com.roomierent.backend.util.SimilarityCalculator;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Grafo de similitud entre propiedades
 * Implementa K-Nearest Neighbors (KNN) para recomendaciones
 */
@Component
public class PropertyGraph {

    // Nodos del grafo: propertyId -> Set de vecinos con similitud
    private final Map<Long, Map<Long, Double>> adjacencyList;

    public PropertyGraph() {
        this.adjacencyList = new HashMap<>();
    }

    /**
     * Agrega una propiedad al grafo
     */
    public void addProperty(Long propertyId) {
        adjacencyList.putIfAbsent(propertyId, new HashMap<>());
    }

    /**
     * Agrega una arista de similitud entre dos propiedades
     */
    public void addSimilarityEdge(Long property1Id, Long property2Id, double similarity) {
        // Agregar arista bidireccional
        adjacencyList.get(property1Id).put(property2Id, similarity);
        adjacencyList.get(property2Id).put(property1Id, similarity);
    }

    /**
     * Encuentra propiedades similares usando BFS modificado
     * (K-Nearest Neighbors en el grafo)
     */
    public List<Long> findSimilarProperties(
            Long propertyId,
            double minSimilarity,
            int limit
    ) {
        if (!adjacencyList.containsKey(propertyId)) {
            return new ArrayList<>();
        }

        // Obtener vecinos directos ordenados por similitud
        Map<Long, Double> neighbors = adjacencyList.get(propertyId);

        List<Map.Entry<Long, Double>> sortedNeighbors = new ArrayList<>(neighbors.entrySet());
        sortedNeighbors.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // Filtrar por similitud mínima y limitar resultados
        List<Long> similarProperties = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : sortedNeighbors) {
            if (entry.getValue() >= minSimilarity && similarProperties.size() < limit) {
                similarProperties.add(entry.getKey());
            }
        }

        return similarProperties;
    }

    /**
     * Calcula la similitud entre dos propiedades
     */
    public static double calculateSimilarity(Property p1, Property p2) {
        // Pesos para cada componente de similitud
        double priceWeight = 0.25;
        double locationWeight = 0.30;
        double amenitiesWeight = 0.20;
        double cosineWeight = 0.25;

        // Componente 1: Similitud de precio
        double priceScore = calculatePriceSimilarity(p1, p2);

        // Componente 2: Similitud de ubicación
        double locationScore = SimilarityCalculator.calculateLocationSimilarity(p1, p2);

        // Componente 3: Similitud de amenities (Jaccard)
        double amenitiesScore = SimilarityCalculator.calculateAmenitiesSimilarity(p1, p2);

        // Componente 4: Similitud de coseno (características generales)
        double cosineScore = SimilarityCalculator.calculateCosineSimilarity(p1, p2);

        // Similitud total ponderada
        return (priceScore * priceWeight) +
                (locationScore * locationWeight) +
                (amenitiesScore * amenitiesWeight) +
                (cosineScore * cosineWeight);
    }

    /**
     * Calcula similitud de precio (Gaussian similarity)
     */
    private static double calculatePriceSimilarity(Property p1, Property p2) {
        if (p1.getPrice() == null || p2.getPrice() == null) {
            return 0.5;
        }

        double price1 = p1.getPrice().doubleValue();
        double price2 = p2.getPrice().doubleValue();

        // Diferencia relativa
        double avgPrice = (price1 + price2) / 2;
        double relativeDiff = Math.abs(price1 - price2) / avgPrice;

        // Función Gaussiana: e^(-(diff^2) / 2σ^2)
        // σ = 0.3 (30% de diferencia da 0.6 de similitud)
        double sigma = 0.3;
        return Math.exp(-(relativeDiff * relativeDiff) / (2 * sigma * sigma));
    }

    /**
     * Limpia el grafo
     */
    public void clear() {
        adjacencyList.clear();
    }

    /**
     * Retorna el número de nodos en el grafo
     */
    public int size() {
        return adjacencyList.size();
    }

    /**
     * Obtiene los vecinos de una propiedad ordenados por similitud
     */
    public List<Map.Entry<Long, Double>> getNeighborsSorted(Long propertyId) {
        if (!adjacencyList.containsKey(propertyId)) {
            return new ArrayList<>();
        }

        List<Map.Entry<Long, Double>> neighbors =
                new ArrayList<>(adjacencyList.get(propertyId).entrySet());

        neighbors.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        return neighbors;
    }
}