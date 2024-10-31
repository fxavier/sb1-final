package com.ecommerce.service;

import com.ecommerce.domain.dto.ReviewDTO;
import com.ecommerce.domain.model.Product;
import com.ecommerce.domain.model.Review;
import com.ecommerce.domain.repository.ReviewRepository;
import com.ecommerce.domain.repository.ProductRepository;
import com.ecommerce.exception.ResourceNotFoundException;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ReviewService reviewService;

    private Review testReview;
    private Product testProduct;
    private ReviewDTO reviewDTO;
    private String userId = "test-user";

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");

        testReview = new Review();
        testReview.setId(1L);
        testReview.setProduct(testProduct);
        testReview.setUserId(userId);
        testReview.setRating(5);
        testReview.setComment("Great product!");
        testReview.setHelpfulVotes(0);
        testReview.setVerifiedPurchase(true);

        reviewDTO = new ReviewDTO();
        reviewDTO.setRating(5);
        reviewDTO.setComment("Great product!");
        reviewDTO.setImageUrls(new HashSet<>());
    }

    @Test
    void getProductReviews_ReturnsReviewsList() {
        List<Review> reviews = Arrays.asList(testReview);
        when(reviewRepository.findByProduct(1L)).thenReturn(Uni.createFrom().item(reviews));

        List<Review> result = reviewService.getProductReviews(1L)
            .await().indefinitely();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(reviewRepository).findByProduct(1L);
    }

    @Test
    void getUserReviews_ReturnsReviewsList() {
        List<Review> reviews = Arrays.asList(testReview);
        when(reviewRepository.findByUser(userId)).thenReturn(Uni.createFrom().item(reviews));

        List<Review> result = reviewService.getUserReviews(userId)
            .await().indefinitely();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(reviewRepository).findByUser(userId);
    }

    @Test
    void createReview_ValidReview_CreatesReview() {
        when(productRepository.findById(1L)).thenReturn(Uni.createFrom().item(testProduct));
        when(reviewRepository.persist(any(Review.class))).thenReturn(Uni.createFrom().item(testReview));
        when(reviewRepository.getAverageRating(1L)).thenReturn(Uni.createFrom().item(5.0));
        when(productRepository.persist(any(Product.class))).thenReturn(Uni.createFrom().item(testProduct));

        Review result = reviewService.createReview(1L, userId, reviewDTO)
            .await().indefinitely();

        assertNotNull(result);
        assertEquals(reviewDTO.getRating(), result.getRating());
        assertEquals(reviewDTO.getComment(), result.getComment());
        verify(productRepository).findById(1L);
        verify(reviewRepository).persist(any(Review.class));
    }

    @Test
    void createReview_NonExistingProduct_ThrowsException() {
        when(productRepository.findById(99L)).thenReturn(Uni.createFrom().nullItem());

        assertThrows(ResourceNotFoundException.class, () -> {
            reviewService.createReview(99L, userId, reviewDTO)
                .await().indefinitely();
        });
        verify(productRepository).findById(99L);
        verify(reviewRepository, never()).persist(any(Review.class));
    }

    @Test
    void updateReview_ValidReview_UpdatesReview() {
        when(reviewRepository.findById(1L)).thenReturn(Uni.createFrom().item(testReview));
        when(reviewRepository.persist(any(Review.class))).thenReturn(Uni.createFrom().item(testReview));
        when(reviewRepository.getAverageRating(1L)).thenReturn(Uni.createFrom().item(5.0));
        when(productRepository.persist(any(Product.class))).thenReturn(Uni.createFrom().item(testProduct));

        reviewDTO.setRating(4);
        reviewDTO.setComment("Updated comment");

        Review result = reviewService.updateReview(1L, userId, reviewDTO)
            .await().indefinitely();

        assertNotNull(result);
        assertEquals(reviewDTO.getRating(), result.getRating());
        assertEquals(reviewDTO.getComment(), result.getComment());
        verify(reviewRepository).findById(1L);
        verify(reviewRepository).persist(any(Review.class));
    }

    @Test
    void updateReview_UnauthorizedUser_ThrowsException() {
        testReview.setUserId("other-user");
        when(reviewRepository.findById(1L)).thenReturn(Uni.createFrom().item(testReview));

        assertThrows(SecurityException.class, () -> {
            reviewService.updateReview(1L, userId, reviewDTO)
                .await().indefinitely();
        });
        verify(reviewRepository).findById(1L);
        verify(reviewRepository, never()).persist(any(Review.class));
    }

    @Test
    void voteHelpful_IncreasesHelpfulVotes() {
        when(reviewRepository.findById(1L)).thenReturn(Uni.createFrom().item(testReview));
        when(reviewRepository.persist(any(Review.class))).thenReturn(Uni.createFrom().item(testReview));

        Review result = reviewService.voteHelpful(1L)
            .await().indefinitely();

        assertNotNull(result);
        assertEquals(1, result.getHelpfulVotes());
        verify(reviewRepository).findById(1L);
        verify(reviewRepository).persist(any(Review.class));
    }
}