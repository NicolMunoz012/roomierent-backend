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

    @Query("SELECT p FROM Property p WHERE p.status = 'DISPONIBLE' ORDER BY p.createdAt DESC")
    List<Property> findAvailableProperties();

    @Query("SELECT p FROM Property p WHERE p.status = 'DISPONIBLE' " +
            "AND p.price BETWEEN :minPrice AND :maxPrice " +
            "AND p.city = :city " +
            "ORDER BY p.createdAt DESC")
    List<Property> findByFilters(
            @Param("city") String city,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    // Método para contar propiedades por propietario
    @Query("SELECT COUNT(p) FROM Property p WHERE p.owner.id = :userId")
    long countByOwnerId(@Param("userId") Long userId);

    // Método para eliminar propiedades por propietario
    @Modifying
    @Transactional
    @Query("DELETE FROM Property p WHERE p.owner.id = :userId")
    void deleteByOwnerId(@Param("userId") Long userId);
}