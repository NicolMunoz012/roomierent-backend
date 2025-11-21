// ReviewRequest.java - Data Transfer Object Pattern
package com.roomierent.backend.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO Pattern: Objeto de transferencia para crear reseñas
 * Builder Pattern: Construcción flexible del objeto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    @NotNull(message = "El ID de la propiedad es requerido")
    private Long propertyId;

    @NotNull(message = "La calificación es requerida")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer rating;

    @Size(max = 1000, message = "El comentario no puede exceder 1000 caracteres")
    private String comment;
}