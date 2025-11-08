package com.roomierent.backend.dto;

import com.roomierent.backend.model.entity.PropertyType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreferencesRequest {

    private String preferredCity;

    private List<String> preferredNeighborhoods;

    @Min(value = 0, message = "El precio mínimo debe ser mayor o igual a 0")
    private BigDecimal minPrice;

    @Min(value = 0, message = "El precio máximo debe ser mayor o igual a 0")
    private BigDecimal maxPrice;

    private PropertyType preferredType;

    @Min(value = 1, message = "Mínimo 1 habitación")
    private Integer minBedrooms;

    @Min(value = 1, message = "Mínimo 1 baño")
    private Integer minBathrooms;

    @Min(value = 0, message = "El área mínima debe ser mayor a 0")
    private Double minArea;

    private List<String> desiredAmenities;
}