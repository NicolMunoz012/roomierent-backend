package com.roomierent.backend.util.datastructures;

import com.roomierent.backend.model.entity.Property;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Graph (Grafo no dirigido) para representar similitud entre propiedades
 *
 * Estructura de Datos: GRAFO
 * Representación: Lista de Adyacencia
 *
 * Uso: Encontrar propiedades similares para recomendaciones
 * Algoritmo: BFS para encontrar propiedades relacionadas
 */
@Component
public class PropertyGraph {

    // Mapa de propertyId -> Lista de propiedades similares con peso de similitud
    private Map<Long, List<Edge>> adjacencyList;

    public PropertyGraph() {
        this.adjacencyList = new HashMap<>();
    }

    /**
     * Clase para representar una arista con peso
     */
    public static class Edge {
        private Long targetPropertyId;
        private double similarityScore; // 0.0 a 1.0

        public Edge(Long targetPropertyId, double similarityScore) {
            this.targetPropertyId = targetPropertyId;
            this.similarityScore = similarityScore;
        }

        public Long getTargetPropertyId() {
            return targetPropertyId;
        }

        public double getSimilarityScore() {
            return similarityScore;
        }
    }

    /**
     * Agrega una propiedad al grafo
     */
    public void addProperty(Long propertyId) {
        adjacencyList.putIfAbsent(propertyId, new ArrayList<>());
    }

    /**
     * Crea una conexión bidireccional entre dos propiedades
     * @param similarity Score de similitud (0.0 a 1.0)
     */
    public void addSimilarityEdge(Long property1Id, Long property2Id, double similarity) {
        addProperty(property1Id);
        addProperty(property2Id);

        adjacencyList.get(property1Id).add(new Edge(property2Id, similarity));
        adjacencyList.get(property2Id).add(new Edge(property1Id, similarity));
    }

    /**
     * Encuentra propiedades similares usando BFS
     * @param propertyId ID de la propiedad origen
     * @param minSimilarity Similitud mínima requerida
     * @param maxResults Número máximo de resultados
     * @return Lista de IDs de propiedades similares ordenadas por similitud
     */
    public List<Long> findSimilarProperties(Long propertyId, double minSimilarity, int maxResults) {
        if (!adjacencyList.containsKey(propertyId)) {
            return new ArrayList<>();
        }

        // Priority Queue para mantener las mejores similitudes
        PriorityQueue<Edge> pq = new PriorityQueue<>(
                (a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore())
        );

        Set<Long> visited = new HashSet<>();
        visited.add(propertyId);

        // BFS
        Queue<Long> queue = new LinkedList<>();
        queue.offer(propertyId);

        while (!queue.isEmpty()) {
            Long current = queue.poll();

            for (Edge edge : adjacencyList.get(current)) {
                if (!visited.contains(edge.getTargetPropertyId()) &&
                        edge.getSimilarityScore() >= minSimilarity) {

                    visited.add(edge.getTargetPropertyId());
                    pq.offer(edge);
                    queue.offer(edge.getTargetPropertyId());
                }
            }
        }

        // Extraer los mejores resultados
        List<Long> results = new ArrayList<>();
        while (!pq.isEmpty() && results.size() < maxResults) {
            results.add(pq.poll().getTargetPropertyId());
        }

        return results;
    }

    /**
     * Calcula la similitud entre dos propiedades basándose en sus características
     */
    public static double calculateSimilarity(Property p1, Property p2) {
        double score = 0.0;

        // Mismo tipo de propiedad (+30%)
        if (p1.getType() == p2.getType()) {
            score += 0.3;
        }

        // Ciudad similar (+20%)
        if (p1.getCity().equalsIgnoreCase(p2.getCity())) {
            score += 0.2;
        }

        // Precio similar (±20%) (+25%)
        double priceDiff = Math.abs(p1.getPrice().doubleValue() - p2.getPrice().doubleValue());
        double avgPrice = (p1.getPrice().doubleValue() + p2.getPrice().doubleValue()) / 2;
        if (avgPrice > 0 && (priceDiff / avgPrice) <= 0.2) {
            score += 0.25;
        }

        // Habitaciones similares (+15%)
        if (Math.abs(p1.getBedrooms() - p2.getBedrooms()) <= 1) {
            score += 0.15;
        }

        // Área similar (±20%) (+10%)
        double areaDiff = Math.abs(p1.getArea() - p2.getArea());
        double avgArea = (p1.getArea() + p2.getArea()) / 2;
        if (avgArea > 0 && (areaDiff / avgArea) <= 0.2) {
            score += 0.1;
        }

        return score;
    }

    public void clear() {
        adjacencyList.clear();
    }

    public int size() {
        return adjacencyList.size();
    }
}