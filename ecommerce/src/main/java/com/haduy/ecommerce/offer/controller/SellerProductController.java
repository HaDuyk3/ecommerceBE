package com.haduy.ecommerce.offer.controller;

import com.haduy.ecommerce.common.enums.SellerProductStatus;
import com.haduy.ecommerce.common.response.ApiResponse;
import com.haduy.ecommerce.offer.dto.SellerProductDto;
import com.haduy.ecommerce.offer.dto.SellerProductRequest;
import com.haduy.ecommerce.offer.dto.SellerProductSearchCriteria;
import com.haduy.ecommerce.offer.service.SellerProductService;
import com.haduy.ecommerce.user.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/seller/products")
@RequiredArgsConstructor
public class SellerProductController {

    private final SellerProductService sellerProductService;

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<SellerProductDto>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SellerProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        sellerProductService.create(userDetails.getId(), request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<SellerProductDto>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody SellerProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerProductService.update(userDetails.getId(), id, request)));
    }

    @GetMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Page<SellerProductDto>>> getMyListings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) SellerProductStatus status,
            @RequestParam(required = false) Boolean inStock,
            @PageableDefault(size = 20, sort = "effectivePrice", direction = Sort.Direction.ASC)
            Pageable pageable) {
        SellerProductSearchCriteria criteria = SellerProductSearchCriteria.builder()
                .productId(productId)
                .status(status)
                .inStock(inStock)
                .build();
        return ResponseEntity.ok(ApiResponse.success(
                sellerProductService.searchMyListings(userDetails.getId(), criteria, pageable)));
    }
}
