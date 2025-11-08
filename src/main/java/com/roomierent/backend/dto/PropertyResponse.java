package com.roomierent.backend.dto;

import com.roomierent.backend.model.entity.PropertyStatus;
import com.roomierent.backend.model.entity.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyResponse {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private PropertyType type;
    private PropertyStatus status;

    private String address;
    private String city;
    private String neighborhood;
    private Double latitude;
    private Double longitude;

    private Integer bedrooms;
    private Integer bathrooms;
    private Double area;

    private List<String> amenities;
    private List<String> imageUrls;

    private Long ownerId;
    private String ownerName;
    private String ownerEmail;

    private Integer viewCount;
    private Integer favoriteCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}