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
public class PropertyRequest {

    @NotBlank(message = "El título es requerido")
    @Size(min = 10, max = 200, message = "El título debe tener entre 10 y 200 caracteres")
    private String title;

    @NotBlank(message = "La descripción es requerida")
    @Size(min = 50, message = "La descripción debe tener al menos 50 caracteres")
    private String description;

    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal price;

    @NotNull(message = "El tipo de propiedad es requerido")
    private PropertyType type;

    @NotBlank(message = "La dirección es requerida")
    private String address;

    @NotBlank(message = "La ciudad es requerida")
    private String city;

    @NotBlank(message = "El barrio es requerido")
    private String neighborhood;

    @NotNull(message = "La latitud es requerida")
    @DecimalMin(value = "-90.0", message = "Latitud inválida")
    @DecimalMax(value = "90.0", message = "Latitud inválida")
    private Double latitude;

    @NotNull(message = "La longitud es requerida")
    @DecimalMin(value = "-180.0", message = "Longitud inválida")
    @DecimalMax(value = "180.0", message = "Longitud inválida")
    private Double longitude;

    @NotNull(message = "El número de habitaciones es requerido")
    @Min(value = 1, message = "Debe tener al menos 1 habitación")
    private Integer bedrooms;

    @NotNull(message = "El número de baños es requerido")
    @Min(value = 1, message = "Debe tener al menos 1 baño")
    private Integer bathrooms;

    @NotNull(message = "El área es requerida")
    @DecimalMin(value = "1.0", message = "El área debe ser mayor a 0")
    private Double area;

    private List<String> amenities;

    private List<String> imageUrls;
}