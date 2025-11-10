package com.roomierent.backend.repository;

import com.roomierent.backend.model.entity.Favorite;
import com.roomierent.backend.model.entity.Property;
import com.roomierent.backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserAndProperty(User user, Property property);

    List<Favorite> findByUser(User user);

    long countByProperty(Property property);

    void deleteByUserAndProperty(User user, Property property);

    @Query("SELECT f.property.id FROM Favorite f WHERE f.user = :user")
    List<Long> findPropertyIdsByUser(@Param("user") User user);
}