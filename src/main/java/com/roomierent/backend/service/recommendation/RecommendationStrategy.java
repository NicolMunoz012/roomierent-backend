package com.roomierent.backend.service.recommendation;

import com.roomierent.backend.model.entity.Property;
import com.roomierent.backend.model.entity.UserPreferences;

import java.util.List;

/**
 * PATRÓN STRATEGY
 *
 * Interfaz para diferentes estrategias de recomendación
 * Permite cambiar el algoritmo de recomendación en tiempo de ejecución
 */
public interface RecommendationStrategy {

    /**
     * Recomienda propiedades basándose en las preferencias del usuario
     *
     * @param availableProperties Lista de propiedades disponibles
     * @param preferences Preferencias del usuario
     * @param limit Número máximo de recomendaciones
     * @return Lista ordenada de propiedades recomendadas
     */
    List<Property> recommend(List<Property> availableProperties,
                             UserPreferences preferences,
                             int limit);

    /**
     * Nombre de la estrategia
     */
    String getStrategyName();
}