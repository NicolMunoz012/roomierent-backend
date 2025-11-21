package com.roomierent.backend.service;

import com.roomierent.backend.dto.PropertyResponse;
import com.roomierent.backend.model.entity.Favorite;
import com.roomierent.backend.model.entity.Property;
import com.roomierent.backend.model.entity.User;
import com.roomierent.backend.repository.FavoriteRepository;
import com.roomierent.backend.repository.PropertyRepository;
import com.roomierent.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyService propertyService;

    public FavoriteService(FavoriteRepository favoriteRepository,
                           UserRepository userRepository,
                           PropertyRepository propertyRepository,
                           PropertyService propertyService) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.propertyService = propertyService;
    }

    @Transactional
    public Map<String, Integer> addFavorite(String userEmail, Long propertyId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Propiedad no encontrada"));

        Optional<Favorite> existing = favoriteRepository.findByUserAndProperty(user, property);
        if (existing.isEmpty()) {
            Favorite favorite = Favorite.builder()
                    .user(user)
                    .property(property)
                    .build();
            favoriteRepository.save(favorite);
            property.incrementFavoriteCount();
            propertyRepository.save(property);
        }

        int count = property.getFavoriteCount();
        return Map.of("favoriteCount", count);
    }

    @Transactional
    public Map<String, Integer> removeFavorite(String userEmail, Long propertyId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Propiedad no encontrada"));

        Optional<Favorite> existing = favoriteRepository.findByUserAndProperty(user, property);
        if (existing.isPresent()) {
            favoriteRepository.deleteByUserAndProperty(user, property);
            property.decrementFavoriteCount();
            if (property.getFavoriteCount() < 0) {
                property.setFavoriteCount(0);
            }
            propertyRepository.save(property);
        }

        int count = property.getFavoriteCount();
        return Map.of("favoriteCount", count);
    }

    public Map<String, Boolean> isFavorite(String userEmail, Long propertyId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Propiedad no encontrada"));

        boolean exists = favoriteRepository.findByUserAndProperty(user, property).isPresent();
        return Map.of("favorite", exists);
    }
    @Transactional(readOnly = true)
    public List<PropertyResponse> getFavorites(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return favoriteRepository.findByUserWithProperty(user).stream()
                .map(Favorite::getProperty)
                .map(propertyService::convertToResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<Long> getFavoriteIds(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return favoriteRepository.findPropertyIdsByUser(user);
    }
}