package com.roomierent.backend.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStats {
    private Double averageRating;
    private Long totalReviews;
    private Integer[] ratingDistribution;
}