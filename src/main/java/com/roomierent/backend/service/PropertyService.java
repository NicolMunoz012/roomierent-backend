package com.roomierent.backend.service;

import com.roomierent.backend.dto.PropertyRequest;
import com.roomierent.backend.dto.PropertyResponse;
import com.roomierent.backend.model.entity.Property;
import com.roomierent.backend.model.entity.PropertyImage;
import com.roomierent.backend.model.entity.PropertyStatus;
import com.roomierent.backend.model.entity.User;
import com.roomierent.backend.repository.PropertyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final UserService userService;

    public PropertyService(PropertyRepository propertyRepository, UserService userService) {
        this.propertyRepository = propertyRepository;
        this.userService = userService;
    }

    /**
     * Crea una nueva propiedad
     */
    @Transactional
    public PropertyResponse createProperty(PropertyRequest request, String ownerEmail) {
        // Buscar el propietario
        User owner = userService.findByEmail(ownerEmail);

        // Convertir lista de amenities a JSON
        String amenitiesJson = request.getAmenities() != null
                ? String.join(",", request.getAmenities())
                : "";

        // Crear la propiedad
        Property property = Property.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .type(request.getType())
                .status(PropertyStatus.AVAILABLE)
                .address(request.getAddress())
                .city(request.getCity())
                .neighborhood(request.getNeighborhood())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .bedrooms(request.getBedrooms())
                .bathrooms(request.getBathrooms())
                .area(request.getArea())
                .amenities(amenitiesJson)
                .owner(owner)
                .images(new ArrayList<>())
                .build();

        // Guardar imágenes si existen
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            int order = 0;
            for (String imageUrl : request.getImageUrls()) {
                PropertyImage image = PropertyImage.builder()
                        .imageUrl(imageUrl)
                        .displayOrder(order++)
                        .isPrimary(order == 1) // Primera imagen es la principal
                        .property(property)
                        .build();
                property.getImages().add(image);
            }
        }

        Property savedProperty = propertyRepository.save(property);

        System.out.println("✅ Propiedad creada: " + savedProperty.getTitle() + " (ID: " + savedProperty.getId() + ")");

        return convertToResponse(savedProperty);
    }

    /**
     * Obtiene todas las propiedades disponibles
     */
    public List<PropertyResponse> getAllAvailableProperties() {
        return propertyRepository.findAvailableProperties().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una propiedad por ID
     */
    public PropertyResponse getPropertyById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Propiedad no encontrada"));

        // Incrementar contador de vistas
        property.incrementViewCount();
        propertyRepository.save(property);

        return convertToResponse(property);
    }

    /**
     * Obtiene las propiedades de un propietario
     */
    public List<PropertyResponse> getPropertiesByOwner(String ownerEmail) {
        User owner = userService.findByEmail(ownerEmail);

        return propertyRepository.findByOwner(owner).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    /**
     * Obtiene el número de propiedades de un propietario
     */
    public long getPropertyCountByOwner(String ownerEmail) {
        User owner = userService.findByEmail(ownerEmail);

        return propertyRepository.countByOwnerId(owner.getId());
    }

    /**
     * Convierte Property a PropertyResponse
     */
    public PropertyResponse convertToResponse(Property property) {
        List<String> amenitiesList = property.getAmenities() != null && !property.getAmenities().isEmpty()
                ? List.of(property.getAmenities().split(","))
                : new ArrayList<>();

        List<String> imageUrls = property.getImages().stream()
                .sorted((a, b) -> a.getDisplayOrder().compareTo(b.getDisplayOrder()))
                .map(PropertyImage::getImageUrl)
                .collect(Collectors.toList());

        return PropertyResponse.builder()
                .id(property.getId())
                .title(property.getTitle())
                .description(property.getDescription())
                .price(property.getPrice())
                .type(property.getType())
                .status(property.getStatus())
                .address(property.getAddress())
                .city(property.getCity())
                .neighborhood(property.getNeighborhood())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .bedrooms(property.getBedrooms())
                .bathrooms(property.getBathrooms())
                .area(property.getArea())
                .amenities(amenitiesList)
                .imageUrls(imageUrls)
                .ownerId(property.getOwner().getId())
                .ownerName(property.getOwner().getName())
                .ownerEmail(property.getOwner().getEmail())
                .viewCount(property.getViewCount())
                .favoriteCount(property.getFavoriteCount())
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .build();
    }
}