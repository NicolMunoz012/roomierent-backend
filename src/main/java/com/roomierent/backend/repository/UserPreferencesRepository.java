package com.roomierent.backend.repository;

import com.roomierent.backend.model.entity.User;
import com.roomierent.backend.model.entity.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {

    Optional<UserPreferences> findByUser(User user);

    Optional<UserPreferences> findByUserId(Long userId);

    boolean existsByUser(User user);

    void deleteByUser(User user);
}