package com.roomierent.backend.service;

import com.roomierent.backend.dto.UserPreferencesRequest;
import com.roomierent.backend.dto.UserPreferencesResponse;
import com.roomierent.backend.model.entity.User;
import com.roomierent.backend.model.entity.UserPreferences;
import com.roomierent.backend.repository.UserPreferencesRepository;
import com.roomierent.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserPreferencesService {

    private final UserPreferencesRepository preferencesRepository;
    private final UserRepository userRepository;

    public UserPreferencesService(UserPreferencesRepository preferencesRepository,
                                  UserRepository userRepository) {
        this.preferencesRepository = preferencesRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public UserPreferencesResponse saveOrUpdatePreferences(String userEmail, UserPreferencesRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserPreferences preferences = preferencesRepository.findByUser(user)
                .orElse(UserPreferences.builder().user(user).build());

        // Actualizar preferencias
        preferences.setPreferredCity(request.getPreferredCity());
        preferences.setPreferredNeighborhoods(
                request.getPreferredNeighborhoods() != null ?
                        String.join(",", request.getPreferredNeighborhoods()) : null
        );
        preferences.setMinPrice(request.getMinPrice());
        preferences.setMaxPrice(request.getMaxPrice());
        preferences.setPreferredType(request.getPreferredType());
        preferences.setMinBedrooms(request.getMinBedrooms());
        preferences.setMinBathrooms(request.getMinBathrooms());
        preferences.setMinArea(request.getMinArea());
        preferences.setDesiredAmenities(
                request.getDesiredAmenities() != null ?
                        String.join(",", request.getDesiredAmenities()) : null
        );

        UserPreferences saved = preferencesRepository.save(preferences);

        return convertToResponse(saved);
    }

    public UserPreferencesResponse getPreferences(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserPreferences preferences = preferencesRepository.findByUser(user)
                .orElse(null);

        return preferences != null ? convertToResponse(preferences) : null;
    }

    private UserPreferencesResponse convertToResponse(UserPreferences preferences) {
        List<String> neighborhoods = preferences.getPreferredNeighborhoods() != null ?
                Arrays.asList(preferences.getPreferredNeighborhoods().split(",")) : null;

        List<String> amenities = preferences.getDesiredAmenities() != null ?
                Arrays.asList(preferences.getDesiredAmenities().split(",")) : null;

        return UserPreferencesResponse.builder()
                .id(preferences.getId())
                .preferredCity(preferences.getPreferredCity())
                .preferredNeighborhoods(neighborhoods)
                .minPrice(preferences.getMinPrice())
                .maxPrice(preferences.getMaxPrice())
                .preferredType(preferences.getPreferredType())
                .minBedrooms(preferences.getMinBedrooms())
                .minBathrooms(preferences.getMinBathrooms())
                .minArea(preferences.getMinArea())
                .desiredAmenities(amenities)
                .createdAt(preferences.getCreatedAt())
                .updatedAt(preferences.getUpdatedAt())
                .build();
    }
}