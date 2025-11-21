package com.roomierent.backend.repository;

import com.roomierent.backend.model.entity.Favorite;
import com.roomierent.backend.model.entity.Property;
import com.roomierent.backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserAndProperty(User user, Property property);

    List<Favorite> findByUser(User user);

    @Query("SELECT f FROM Favorite f " +
            "JOIN FETCH f.property p " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.owner " +
            "WHERE f.user = :user " +
            "ORDER BY f.createdAt DESC")

    List<Favorite> findByUserWithProperty(@Param("user") User user);

    @Modifying
    @Transactional
    void deleteByUserAndProperty(User user, Property property);

    @Query("SELECT f.property.id FROM Favorite f WHERE f.user = :user")
    List<Long> findPropertyIdsByUser(@Param("user") User user);
}