package com.roomierent.backend.repository;

import com.roomierent.backend.model.entity.Property;
import com.roomierent.backend.model.entity.PropertyStatus;
import com.roomierent.backend.model.entity.PropertyType;
import com.roomierent.backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findByOwner(User owner);

    List<Property> findByStatus(PropertyStatus status);

    List<Property> findByCity(String city);

    List<Property> findByType(PropertyType type);

    /**
     * Obtiene todas las propiedades disponibles ordenadas por fecha
     */
    @Query("SELECT p FROM Property p WHERE p.status = 'AVAILABLE' ORDER BY p.createdAt DESC")
    List<Property> findAvailableProperties();

    /**
     * Búsqueda avanzada con filtros múltiples
     */
    @Query("SELECT p FROM Property p WHERE p.status = 'AVAILABLE' " +
            "AND p.price BETWEEN :minPrice AND :maxPrice " +
            "AND p.city = :city " +
            "ORDER BY p.createdAt DESC")
    List<Property> findByFilters(
            @Param("city") String city,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    /**
     * Búsqueda por ciudad y tipo
     */
    @Query("SELECT p FROM Property p WHERE p.status = 'AVAILABLE' " +
            "AND p.city = :city " +
            "AND p.type = :type " +
            "ORDER BY p.price ASC")
    List<Property> findByCityAndType(
            @Param("city") String city,
            @Param("type") PropertyType type
    );

    /**
     * Búsqueda por rango de precio y habitaciones
     */
    @Query("SELECT p FROM Property p WHERE p.status = 'AVAILABLE' " +
            "AND p.price BETWEEN :minPrice AND :maxPrice " +
            "AND p.bedrooms >= :minBedrooms " +
            "ORDER BY p.price ASC")
    List<Property> findByPriceAndBedrooms(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("minBedrooms") Integer minBedrooms
    );

    /**
     * Propiedades más vistas (trending)
     */
    @Query("SELECT p FROM Property p WHERE p.status = 'AVAILABLE' " +
            "ORDER BY p.viewCount DESC, p.favoriteCount DESC")
    List<Property> findTrendingProperties();

    /**
     * Propiedades en un barrio específico
     */
    @Query("SELECT p FROM Property p WHERE p.status = 'AVAILABLE' " +
            "AND p.neighborhood = :neighborhood " +
            "ORDER BY p.createdAt DESC")
    List<Property> findByNeighborhood(@Param("neighborhood") String neighborhood);

    /**
     * Cuenta propiedades de un propietario
     */
    @Query("SELECT COUNT(p) FROM Property p WHERE p.owner.id = :userId")
    long countByOwnerId(@Param("userId") Long userId);

    /**
     * Elimina todas las propiedades de un propietario
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Property p WHERE p.owner.id = :userId")
    void deleteByOwnerId(@Param("userId") Long userId);

    /**
     * Búsqueda por área mínima
     */
    @Query("SELECT p FROM Property p WHERE p.status = 'AVAILABLE' " +
            "AND p.area >= :minArea " +
            "ORDER BY p.area DESC")
    List<Property> findByMinArea(@Param("minArea") Double minArea);

    @Query("SELECT DISTINCT p FROM Property p " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.owner " +
            "WHERE p.status = 'AVAILABLE'")
    List<Property> findAvailablePropertiesWithOwner();
}