package com.haduy.ecommerce.review.service;

import com.haduy.ecommerce.common.exception.BusinessException;
import com.haduy.ecommerce.common.exception.ErrorCode;
import com.haduy.ecommerce.order.entity.Order;
import com.haduy.ecommerce.order.entity.OrderItem;
import com.haduy.ecommerce.order.repository.OrderRepository;
//import com.haduy.ecommerce.order.service.OrderService;
import com.haduy.ecommerce.product.service.ProductService;
import com.haduy.ecommerce.review.dto.ReviewDto;
import com.haduy.ecommerce.review.dto.ReviewRequest;
import com.haduy.ecommerce.review.entity.Review;
import com.haduy.ecommerce.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    //private final OrderService orderService;
    private final ProductService productService;
    private final OrderRepository orderRepository;

    @Transactional
    public ReviewDto createReview(UUID userId, ReviewRequest request) {
        // Guard duplicate — mỗi orderItem chỉ review 1 lần
        if (reviewRepository.existsByOrderItemId(request.getOrderItemId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_REVIEW);
        }

        // Validate orderItem thuộc về user và đã được mua
        Order order = findOrderByOrderItemId(userId, request.getOrderItemId());
        OrderItem orderItem = order.getItems().stream()
                .filter(item -> item.getId().equals(request.getOrderItemId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_ALLOWED));

        Review review = Review.builder()
                .orderItem(orderItem)
                .rating(request.getRating())
                .comment(request.getComment())
                .productId(orderItem.getSellerProductId())
                .userId(userId)
                .build();

        Review saved = reviewRepository.save(review);

        // Cập nhật avgRating async — không block response
        updateProductRatingAsync(orderItem.getSellerProductId());

        return ReviewDto.from(saved);
    }

    public Page<ReviewDto> getByProductId(UUID productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable)
                .map(ReviewDto::from);
    }

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

    // Xóa method findOrderByOrderItemId cũ, thay bằng:
    private Order findOrderByOrderItemId(UUID userId, UUID orderItemId) {
        return orderRepository.findByItemIdAndUserId(orderItemId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_ALLOWED));
    }
}