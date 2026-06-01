package com.haduy.ecommerce.offer.controller;

import com.haduy.ecommerce.common.response.ApiResponse;
import com.haduy.ecommerce.offer.dto.SellerProductDto;
import com.haduy.ecommerce.offer.dto.SellerProductRequest;
import com.haduy.ecommerce.offer.service.SellerProductService;
import com.haduy.ecommerce.user.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SellerProductController {

    private final SellerProductService sellerProductService;

    @PostMapping("/api/seller/products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<SellerProductDto>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SellerProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        sellerProductService.create(userDetails.getId(), request)));
    }

    @PutMapping("/api/seller/products/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<SellerProductDto>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody SellerProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerProductService.update(userDetails.getId(), id, request)));
    }

    @GetMapping("/api/seller/products")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<List<SellerProductDto>>> getMyListings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerProductService.getMyListings(userDetails.getId())));
    }

    @GetMapping("/api/products/{productId}/sellers")
    public ResponseEntity<ApiResponse<List<SellerProductDto>>> getByProduct(
            @PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerProductService.getByProductId(productId)));
    }
}