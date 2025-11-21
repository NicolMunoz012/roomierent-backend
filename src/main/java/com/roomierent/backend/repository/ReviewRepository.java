package com.roomierent.backend.repository;

import com.roomierent.backend.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);

    Optional<Review> findByPropertyIdAndUserId(Long propertyId, Long userId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.property.id = ?1")
    Double getAverageRatingByPropertyId(Long propertyId);

    Long countByPropertyId(Long propertyId);

    boolean existsByPropertyIdAndUserId(Long propertyId, Long userId);
}