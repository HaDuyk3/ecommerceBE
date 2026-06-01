package com.haduy.ecommerce.review.controller;

import com.haduy.ecommerce.common.response.ApiResponse;
import com.haduy.ecommerce.review.dto.ReviewDto;
import com.haduy.ecommerce.review.dto.ReviewRequest;
import com.haduy.ecommerce.review.service.ReviewService;
import com.haduy.ecommerce.user.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/reviews")
    public ResponseEntity<ApiResponse<ReviewDto>> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        reviewService.createReview(userDetails.getId(), request)));
    }

    @GetMapping("/api/products/{productId}/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getByProduct(
            @PathVariable UUID productId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getByProductId(productId, pageable)));
    }
}