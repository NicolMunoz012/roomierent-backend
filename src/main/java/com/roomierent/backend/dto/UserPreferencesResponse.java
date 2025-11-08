package com.roomierent.backend.dto;

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
public class UserPreferencesResponse {

    private Long id;
    private String preferredCity;
    private List<String> preferredNeighborhoods;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private PropertyType preferredType;
    private Integer minBedrooms;
    private Integer minBathrooms;
    private Double minArea;
    private List<String> desiredAmenities;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}