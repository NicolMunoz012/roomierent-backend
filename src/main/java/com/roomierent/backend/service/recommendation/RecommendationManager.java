package com.roomierent.backend.service.recommendation;

import com.roomierent.backend.model.entity.Property;
import com.roomierent.backend.model.entity.User;
import com.roomierent.backend.model.entity.UserPreferences;
import com.roomierent.backend.repository.PropertyRepository;
import com.roomierent.backend.repository.UserPreferencesRepository;
import com.roomierent.backend.repository.UserRepository;
import com.roomierent.backend.util.datastructures.PropertyGraph;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class RecommendationManager {

    private final UserRepository userRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyGraph propertyGraph;
    private final List<RecommendationStrategy> strategies;
    private RecommendationStrategy currentStrategy;

    public RecommendationManager(
            UserRepository userRepository,
            UserPreferencesRepository preferencesRepository,
            PropertyRepository propertyRepository,
            PropertyGraph propertyGraph,
            ScoreBasedRecommendationStrategy scoreBasedStrategy) {

        this.userRepository = userRepository;
        this.preferencesRepository = preferencesRepository;
        this.propertyRepository = propertyRepository;
        this.propertyGraph = propertyGraph;

        // Registrar estrategias disponibles
        this.strategies = new ArrayList<>();
        this.strategies.add(scoreBasedStrategy);

        // Estrategia por defecto
        this.currentStrategy = scoreBasedStrategy;
    }

    /**
     * Obtiene recomendaciones personalizadas para un usuario
     *
     * @param userEmail Email del usuario
     * @param limit Número máximo de recomendaciones
     * @return Lista de propiedades recomendadas
     */
    public List<Property> getRecommendationsForUser(String userEmail, int limit) {
        System.out.println("🎯 Generando recomendaciones para: " + userEmail);

        // 1. Buscar usuario
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Buscar preferencias del usuario
        UserPreferences preferences = preferencesRepository.findByUser(user)
                .orElse(createDefaultPreferences(user));

        // 3. Obtener propiedades disponibles
        List<Property> availableProperties = propertyRepository.findAvailableProperties();

        System.out.println("   📊 Propiedades disponibles: " + availableProperties.size());
        System.out.println("   🎲 Estrategia: " + currentStrategy.getStrategyName());

        // 4. Aplicar estrategia de recomendación
        List<Property> recommendations = currentStrategy.recommend(
                availableProperties,
                preferences,
                limit
        );

        System.out.println("   ✅ Recomendaciones generadas: " + recommendations.size());

        return recommendations;
    }

    public List<Property> getSimilarProperties(Long propertyId, int limit) {
        System.out.println("🔍 Buscando propiedades similares a ID: " + propertyId);

        // Construir grafo si está vacío
        if (propertyGraph.size() == 0) {
            buildPropertyGraph();
        }

        // Buscar propiedades similares usando BFS en el grafo
        List<Long> similarPropertyIds = propertyGraph.findSimilarProperties(
                propertyId,
                0.3, // Similitud mínima 30%
                limit
        );

        // Convertir IDs a entidades Property
        List<Property> similarProperties = new ArrayList<>();
        for (Long id : similarPropertyIds) {
            propertyRepository.findById(id).ifPresent(similarProperties::add);
        }

        System.out.println("   ✅ Propiedades similares encontradas: " + similarProperties.size());

        return similarProperties;
    }

    /**
     * Construye el grafo de similitud entre todas las propiedades
     * Este proceso se ejecuta cuando es necesario
     */
    public void buildPropertyGraph() {
        System.out.println("🔨 Construyendo grafo de similitud de propiedades...");

        propertyGraph.clear();
        List<Property> allProperties = propertyRepository.findAll();

        // Agregar todas las propiedades al grafo
        for (Property property : allProperties) {
            propertyGraph.addProperty(property.getId());
        }

        // Calcular similitud entre cada par de propiedades
        int edges = 0;
        for (int i = 0; i < allProperties.size(); i++) {
            for (int j = i + 1; j < allProperties.size(); j++) {
                Property p1 = allProperties.get(i);
                Property p2 = allProperties.get(j);

                double similarity = PropertyGraph.calculateSimilarity(p1, p2);

                // Solo agregar arista si hay similitud significativa (> 20%)
                if (similarity > 0.2) {
                    propertyGraph.addSimilarityEdge(p1.getId(), p2.getId(), similarity);
                    edges++;
                }
            }
        }

        System.out.println("   ✅ Grafo construido: " +
                allProperties.size() + " nodos, " +
                edges + " aristas");
    }

    /**
     * Cambia la estrategia de recomendación actual
     *
     * @param strategyName Nombre de la estrategia
     */
    public void setStrategy(String strategyName) {
        for (RecommendationStrategy strategy : strategies) {
            if (strategy.getStrategyName().equalsIgnoreCase(strategyName)) {
                this.currentStrategy = strategy;
                System.out.println("🔄 Estrategia cambiada a: " + strategyName);
                return;
            }
        }
        throw new RuntimeException("Estrategia no encontrada: " + strategyName);
    }

    /**
     * Lista las estrategias disponibles
     */
    public List<String> getAvailableStrategies() {
        List<String> strategyNames = new ArrayList<>();
        for (RecommendationStrategy strategy : strategies) {
            strategyNames.add(strategy.getStrategyName());
        }
        return strategyNames;
    }

    /**
     * Crea preferencias por defecto para un usuario nuevo
     */
    private UserPreferences createDefaultPreferences(User user) {
        return UserPreferences.builder()
                .user(user)
                .minBedrooms(1)
                .minBathrooms(1)
                .priceWeight(0.3)
                .locationWeight(0.25)
                .amenitiesWeight(0.2)
                .sizeWeight(0.15)
                .typeWeight(0.1)
                .build();
    }
}