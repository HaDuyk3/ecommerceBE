package com.haduy.ecommerce.review.dto;

import com.haduy.ecommerce.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ReviewDto {
    private UUID id;
    private UUID productId;
    private UUID userId;
    private UUID orderItemId;
    private Integer rating;
    private String comment;
    private Instant createdAt;

    public static ReviewDto from(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .userId(review.getUserId())
                .orderItemId(review.getOrderItem().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}