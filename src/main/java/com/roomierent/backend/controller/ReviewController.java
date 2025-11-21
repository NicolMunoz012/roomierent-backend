package com.roomierent.backend.controller;

import com.roomierent.backend.dto.ReviewRequest;
import com.roomierent.backend.dto.ReviewResponse;
import com.roomierent.backend.dto.ReviewStats;
import com.roomierent.backend.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller Pattern: Maneja requests HTTP
 * Facade Pattern: Simplifica acceso al servicio
 */
@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(
        origins = {
                "http://localhost:3000",
                "https://roomierent-frontend.vercel.app",
                "https://*.vercel.app",
                "https://www.roomierent.online/"
        },
        allowedHeaders = "*",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.DELETE,
                RequestMethod.OPTIONS
        },
        allowCredentials = "true"
)
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<ReviewResponse>> getReviews(
            @PathVariable Long propertyId,
            Authentication authentication
    ) {
        try {
            String email = authentication != null ? authentication.getName() : null;
            List<ReviewResponse> reviews = reviewService.getReviewsByProperty(propertyId, email);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/property/{propertyId}/stats")
    public ResponseEntity<ReviewStats> getReviewStats(@PathVariable Long propertyId) {
        try {
            ReviewStats stats = reviewService.getReviewStats(propertyId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> addReview(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication
    ) {
        try {
            String email = authentication.getName();
            ReviewResponse review = reviewService.addReview(email, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication
    ) {
        try {
            String email = authentication.getName();
            reviewService.deleteReview(reviewId, email);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Reseña eliminada");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}