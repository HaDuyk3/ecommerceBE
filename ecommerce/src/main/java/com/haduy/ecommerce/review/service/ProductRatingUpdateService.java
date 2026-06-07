package com.haduy.ecommerce.review.service;

import com.haduy.ecommerce.product.service.ProductService;
import com.haduy.ecommerce.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRatingUpdateService {

    private final ReviewRepository reviewRepository;
    private final ProductService productService;

    @Async("taskExecutor")
    public void updateProductRatingAsync(UUID productId) {
        try {
            Double avgRating = reviewRepository.calculateAvgRating(productId);
            Integer reviewCount = reviewRepository.countByProductId(productId);
            if (avgRating != null) {
                productService.updateRating(productId, avgRating, reviewCount);
                log.info("Updated rating for product {}: avg={}, count={}",
                        productId, avgRating, reviewCount);
            }
        } catch (Exception e) {
            log.error("Failed to update rating for product {}", productId, e);
        }
    }
}
