package com.roomierent.backend.service;

import com.roomierent.backend.dto.ReviewRequest;
import com.roomierent.backend.dto.ReviewResponse;
import com.roomierent.backend.dto.ReviewStats;
import com.roomierent.backend.model.entity.Property;
import com.roomierent.backend.model.entity.Review;
import com.roomierent.backend.model.entity.User;
import com.roomierent.backend.repository.PropertyRepository;
import com.roomierent.backend.repository.ReviewRepository;
import com.roomierent.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            PropertyRepository propertyRepository,
            UserRepository userRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
    }

    /**
     * Strategy Pattern: Obtener reseñas con diferentes estrategias de ordenamiento
     */
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProperty(Long propertyId, String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail).orElse(null);

        return reviewRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId)
                .stream()
                .map(review -> convertToResponse(review, currentUser))
                .collect(Collectors.toList());
    }

    /**
     * Value Object Pattern: Retorna objeto inmutable con estadísticas
     */
    @Transactional(readOnly = true)
    public ReviewStats getReviewStats(Long propertyId) {
        Double average = reviewRepository.getAverageRatingByPropertyId(propertyId);
        Long count = reviewRepository.countByPropertyId(propertyId);

        // Calcular distribución de ratings
        List<Review> reviews = reviewRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId);
        Integer[] distribution = calculateRatingDistribution(reviews);

        return ReviewStats.builder()
                .averageRating(average != null ? Math.round(average * 10.0) / 10.0 : 0.0)
                .totalReviews(count)
                .ratingDistribution(distribution)
                .build();
    }

    /**
     * Template Method Pattern: Algoritmo de cálculo de distribución
     */
    private Integer[] calculateRatingDistribution(List<Review> reviews) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i = 1; i <= 5; i++) counts.put(i, 0);

        for (Review review : reviews) {
            counts.merge(review.getRating(), 1, Integer::sum);
        }

        return new Integer[]{
                counts.get(1), counts.get(2), counts.get(3),
                counts.get(4), counts.get(5)
        };
    }

    /**
     * Factory Method Pattern: Creación de reseñas
     * Chain of Responsibility Pattern: Validaciones en cadena
     */
    @Transactional
    public ReviewResponse addReview(String userEmail, ReviewRequest request) {
        // 1. Validar usuario
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. Validar propiedad
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new RuntimeException("Propiedad no encontrada"));

        // 3. Validar duplicados
        if (reviewRepository.existsByPropertyIdAndUserId(property.getId(), user.getId())) {
            throw new RuntimeException("Ya has dejado una reseña para esta propiedad");
        }

        // 4. Validar propietario
        if (property.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("No puedes reseñar tu propia propiedad");
        }

        // 5. Crear reseña (Factory Method)
        Review review = Review.builder()
                .property(property)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review savedReview = reviewRepository.save(review);

        System.out.println("✅ Reseña creada: " + savedReview.getId());

        return convertToResponse(savedReview, user);
    }

    /**
     * Command Pattern: Comando de eliminación de reseña
     */
    @Transactional
    public void deleteReview(Long reviewId, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Strategy Pattern: Solo el autor puede eliminar
        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("No tienes permiso para eliminar esta reseña");
        }

        reviewRepository.delete(review);

        System.out.println("✅ Reseña eliminada: " + reviewId);
    }

    /**
     * Adapter Pattern: Convierte Entity a DTO
     */
    private ReviewResponse convertToResponse(Review review, User currentUser) {
        boolean canDelete = currentUser != null &&
                review.getUser().getId().equals(currentUser.getId());

        return ReviewResponse.builder()
                .id(review.getId())
                .propertyId(review.getProperty().getId())
                .userId(review.getUser().getId())
                .userName(review.getUser().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .canDelete(canDelete)
                .build();
    }
}