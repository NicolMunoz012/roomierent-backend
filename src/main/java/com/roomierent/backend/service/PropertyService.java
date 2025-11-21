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
    @Transactional(readOnly = true)
    public List<PropertyResponse> getAllAvailableProperties() {
        return propertyRepository.findAvailableProperties().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una propiedad por ID
     */
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByOwner(String ownerEmail) {
        User owner = userService.findByEmail(ownerEmail);

        return propertyRepository.findByOwner(owner).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    public void deleteProperty(Long id, String email) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        if (!property.getOwner().getEmail().equals(email)) {
            throw new RuntimeException("You are not allowed to delete this property");
        }

        propertyRepository.delete(property);
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
        try {
            List<String> amenitiesList = new ArrayList<>();
            if (property.getAmenities() != null && !property.getAmenities().isEmpty()) {
                amenitiesList = List.of(property.getAmenities().split(","));
            }

            // ✅ Images seguro
            List<String> imageUrls = new ArrayList<>();
            if (property.getImages() != null && !property.getImages().isEmpty()) {
                imageUrls = property.getImages().stream()
                        .sorted((a, b) -> Integer.compare(
                                a.getDisplayOrder() != null ? a.getDisplayOrder() : 0,
                                b.getDisplayOrder() != null ? b.getDisplayOrder() : 0
                        ))
                        .map(PropertyImage::getImageUrl)
                        .filter(url -> url != null && !url.isEmpty())
                        .collect(Collectors.toList());
            }

            Long ownerId = null;
            String ownerName = "Desconocido";
            String ownerEmail = "N/A";

            if (property.getOwner() != null) {
                ownerId = property.getOwner().getId();
                ownerName = property.getOwner().getName() != null ? property.getOwner().getName() : "Desconocido";
                ownerEmail = property.getOwner().getEmail() != null ? property.getOwner().getEmail() : "N/A";
            }

            return PropertyResponse.builder()
                    .id(property.getId())
                    .title(property.getTitle())
                    .description(property.getDescription())
                    .price(property.getPrice())
                    .type(property.getType())
                    .status(property.getStatus() != null ? property.getStatus() : PropertyStatus.AVAILABLE)
                    .address(property.getAddress())
                    .city(property.getCity())
                    .neighborhood(property.getNeighborhood())
                    .latitude(property.getLatitude() != null ? property.getLatitude() : 0.0)
                    .longitude(property.getLongitude() != null ? property.getLongitude() : 0.0)
                    .bedrooms(property.getBedrooms() != null ? property.getBedrooms() : 0)
                    .bathrooms(property.getBathrooms() != null ? property.getBathrooms() : 0)
                    .area(property.getArea() != null ? property.getArea() : 0.0)
                    .amenities(amenitiesList)
                    .imageUrls(imageUrls)
                    .ownerId(ownerId)
                    .ownerName(ownerName)
                    .ownerEmail(ownerEmail)
                    .viewCount(property.getViewCount() != null ? property.getViewCount() : 0)
                    .favoriteCount(property.getFavoriteCount() != null ? property.getFavoriteCount() : 0)
                    .createdAt(property.getCreatedAt())
                    .updatedAt(property.getUpdatedAt())
                    .build();

        } catch (Exception e) {
            System.out.println("❌❌❌ Error en convertToResponse para propiedad ID: " + property.getId());
            System.out.println("❌ Mensaje: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al convertir propiedad a respuesta", e);
        }
    }
}