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

/**
 * Manager principal del sistema de recomendaciones con IA
 * Coordina diferentes estrategias de Machine Learning
 */
@Service
public class RecommendationManager {

    private final UserRepository userRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyGraph propertyGraph;
    private final List<PropertyRecommender> strategies;
    private PropertyRecommender currentStrategy;

    public RecommendationManager(
            UserRepository userRepository,
            UserPreferencesRepository preferencesRepository,
            PropertyRepository propertyRepository,
            PropertyGraph propertyGraph,
            ScoreBasedPropertyRecommender scoreBasedStrategy) {

        this.userRepository = userRepository;
        this.preferencesRepository = preferencesRepository;
        this.propertyRepository = propertyRepository;
        this.propertyGraph = propertyGraph;

        // Registrar estrategias disponibles
        this.strategies = new ArrayList<>();
        this.strategies.add(scoreBasedStrategy);

        // Estrategia por defecto
        this.currentStrategy = scoreBasedStrategy;

        System.out.println("✅ RecommendationManager inicializado");
        System.out.println("   🤖 Estrategia activa: " + currentStrategy.getStrategyName());
    }

    /**
     * Obtiene recomendaciones personalizadas para un usuario
     *
     * @param userEmail Email del usuario
     * @param limit Número máximo de recomendaciones
     * @return Lista de propiedades recomendadas
     */
    public List<Property> getRecommendationsForUser(String userEmail, int limit) {
        System.out.println("\n🎯 ============================================");
        System.out.println("   GENERANDO RECOMENDACIONES CON IA");
        System.out.println("   ============================================");
        System.out.println("   👤 Usuario: " + userEmail);

        // 1. Buscar usuario
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Buscar preferencias del usuario
        UserPreferences preferences = preferencesRepository.findByUser(user)
                .orElse(createDefaultPreferences(user));

        System.out.println("   ⚙️  Preferencias cargadas:");
        System.out.println("      • Ciudad: " + preferences.getPreferredCity());
        System.out.println("      • Precio: " + preferences.getMinPrice() + " - " + preferences.getMaxPrice());
        System.out.println("      • Habitaciones mín: " + preferences.getMinBedrooms());
        System.out.println("      • Pesos: [Precio:" + preferences.getPriceWeight() +
                ", Ubicación:" + preferences.getLocationWeight() +
                ", Amenities:" + preferences.getAmenitiesWeight() + "]");

        // 3. Obtener propiedades disponibles
        List<Property> availableProperties = propertyRepository.findAvailableProperties();

        System.out.println("   📊 Propiedades disponibles: " + availableProperties.size());
        System.out.println("   🤖 Estrategia: " + currentStrategy.getStrategyName());

        // 4. Aplicar estrategia de recomendación (IA)
        List<Property> recommendations = currentStrategy.recommend(
                availableProperties,
                preferences,
                limit
        );

        System.out.println("   ✅ Recomendaciones generadas: " + recommendations.size());
        System.out.println("   ============================================\n");

        return recommendations;
    }

    /**
     * Obtiene propiedades similares usando el grafo de similitud (KNN)
     */
    public List<Property> getSimilarProperties(Long propertyId, int limit) {
        System.out.println("\n🔍 ============================================");
        System.out.println("   BUSCANDO PROPIEDADES SIMILARES (KNN)");
        System.out.println("   ============================================");
        System.out.println("   🏠 Propiedad base ID: " + propertyId);

        // Construir grafo si está vacío
        if (propertyGraph.size() == 0) {
            System.out.println("   ⚠️  Grafo vacío, construyendo...");
            buildPropertyGraph();
        }

        // Buscar propiedades similares usando KNN en el grafo
        List<Long> similarPropertyIds = propertyGraph.findSimilarProperties(
                propertyId,
                0.3, // Similitud mínima 30%
                limit
        );

        System.out.println("   📊 IDs similares encontrados: " + similarPropertyIds.size());

        // Convertir IDs a entidades Property
        List<Property> similarProperties = new ArrayList<>();
        for (Long id : similarPropertyIds) {
            propertyRepository.findById(id).ifPresent(property -> {
                similarProperties.add(property);
                System.out.println("      • " + property.getTitle() + " (ID: " + id + ")");
            });
        }

        System.out.println("   ✅ Propiedades similares: " + similarProperties.size());
        System.out.println("   ============================================\n");

        return similarProperties;
    }

    /**
     * Construye el grafo de similitud entre todas las propiedades
     * Usa algoritmos de Machine Learning para calcular similitudes
     */
    public void buildPropertyGraph() {
        System.out.println("\n🔨 ============================================");
        System.out.println("   CONSTRUYENDO GRAFO DE SIMILITUD (ML)");
        System.out.println("   ============================================");

        propertyGraph.clear();
        List<Property> allProperties = propertyRepository.findAll();

        System.out.println("   📊 Total de propiedades: " + allProperties.size());

        // Agregar todas las propiedades al grafo
        for (Property property : allProperties) {
            propertyGraph.addProperty(property.getId());
        }

        // Calcular similitud entre cada par de propiedades
        int edges = 0;
        int comparisons = 0;

        for (int i = 0; i < allProperties.size(); i++) {
            for (int j = i + 1; j < allProperties.size(); j++) {
                Property p1 = allProperties.get(i);
                Property p2 = allProperties.get(j);

                // Calcular similitud usando algoritmos de ML
                double similarity = PropertyGraph.calculateSimilarity(p1, p2);
                comparisons++;

                // Solo agregar arista si hay similitud significativa (> 20%)
                if (similarity > 0.2) {
                    propertyGraph.addSimilarityEdge(p1.getId(), p2.getId(), similarity);
                    edges++;
                }
            }
        }

        System.out.println("   ✅ Grafo construido exitosamente:");
        System.out.println("      • Nodos (propiedades): " + allProperties.size());
        System.out.println("      • Comparaciones realizadas: " + comparisons);
        System.out.println("      • Aristas (similitudes > 20%): " + edges);
        System.out.println("      • Densidad del grafo: " +
                String.format("%.2f%%", (edges * 100.0) / comparisons));
        System.out.println("   ============================================\n");
    }

    /**
     * Cambia la estrategia de recomendación actual
     *
     * @param strategyName Nombre de la estrategia
     */
    public void setStrategy(String strategyName) {
        for (PropertyRecommender strategy : strategies) {
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
        for (PropertyRecommender strategy : strategies) {
            strategyNames.add(strategy.getStrategyName());
        }
        return strategyNames;
    }

    /**
     * Crea preferencias por defecto para un usuario nuevo
     */
    private UserPreferences createDefaultPreferences(User user) {
        System.out.println("   ⚠️  Usuario sin preferencias, usando valores por defecto");

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