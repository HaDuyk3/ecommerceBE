package com.haduy.ecommerce.review.service;

import com.haduy.ecommerce.common.enums.OrderStatus;
import com.haduy.ecommerce.common.exception.BusinessException;
import com.haduy.ecommerce.common.exception.ErrorCode;
import com.haduy.ecommerce.offer.entity.SellerProduct;
import com.haduy.ecommerce.offer.service.SellerProductService;
import com.haduy.ecommerce.order.entity.Order;
import com.haduy.ecommerce.order.entity.OrderItem;
import com.haduy.ecommerce.order.repository.OrderRepository;
import com.haduy.ecommerce.review.dto.ReviewDto;
import com.haduy.ecommerce.review.dto.ReviewRequest;
import com.haduy.ecommerce.review.entity.Review;
import com.haduy.ecommerce.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final SellerProductService sellerProductService;
    private final OrderRepository orderRepository;
    private final ProductRatingUpdateService productRatingUpdateService;

    @Transactional
    public ReviewDto createReview(UUID userId, ReviewRequest request) {
        if (reviewRepository.existsByOrderItemId(request.getOrderItemId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_REVIEW);
        }

        Order order = findOrderByOrderItemId(userId, request.getOrderItemId());
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_ALLOWED);
        }

        OrderItem orderItem = order.getItems().stream()
                .filter(item -> item.getId().equals(request.getOrderItemId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_ALLOWED));

        SellerProduct sellerProduct = sellerProductService.findOrThrow(orderItem.getSellerProductId());
        UUID productId = sellerProduct.getProduct().getId();

        Review review = Review.builder()
                .orderItem(orderItem)
                .rating(request.getRating())
                .comment(request.getComment())
                .productId(productId)
                .userId(userId)
                .build();

        Review saved = reviewRepository.save(review);
        productRatingUpdateService.updateProductRatingAsync(productId);

        return ReviewDto.from(saved);
    }

    public Page<ReviewDto> getByProductId(UUID productId, Pageable pageable) {
        return reviewRepository.findByProductId(productId, pageable)
                .map(ReviewDto::from);
    }

    private Order findOrderByOrderItemId(UUID userId, UUID orderItemId) {
        return orderRepository.findByItemIdAndUserId(orderItemId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_ALLOWED));
    }
}
