// ReviewResponse.java - DTO Pattern
package com.roomierent.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO Pattern: Respuesta optimizada para el cliente
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long propertyId;
    private Long userId;
    private String userName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private boolean canDelete; // Strategy Pattern: indica si el usuario puede eliminar
}