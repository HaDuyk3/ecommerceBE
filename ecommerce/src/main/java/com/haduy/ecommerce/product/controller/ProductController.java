package com.haduy.ecommerce.product.controller;

import com.haduy.ecommerce.common.response.ApiResponse;
import com.haduy.ecommerce.offer.dto.SellerProductDto;
import com.haduy.ecommerce.offer.service.SellerProductService;
import com.haduy.ecommerce.product.dto.ProductDto;
import com.haduy.ecommerce.product.dto.ProductRequest;
import com.haduy.ecommerce.product.dto.ProductSearchCriteria;
import com.haduy.ecommerce.product.service.ProductService;
import com.haduy.ecommerce.review.dto.ReviewDto;
import com.haduy.ecommerce.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final SellerProductService sellerProductService;
    private final ReviewService reviewService;

    /**
     * Sort: lowestPrice, avgRating, createdAt, name, reviewCount, totalSold (asc/desc).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductDto>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean inStock,
            @PageableDefault(size = 20, sort = "lowestPrice", direction = Sort.Direction.ASC)
            Pageable pageable) {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .brand(brand)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .inStock(inStock)
                .build();
        return ResponseEntity.ok(ApiResponse.success(productService.searchPublic(criteria, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getById(id)));
    }

    @GetMapping("/{productId}/sellers")
    public ResponseEntity<ApiResponse<List<SellerProductDto>>> getSellersByProduct(
            @PathVariable UUID productId) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerProductService.getByProductId(productId)));
    }

    @GetMapping("/{productId}/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewDto>>> getReviewsByProduct(
            @PathVariable UUID productId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                reviewService.getByProductId(productId, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductDto>> create(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(productService.create(request)));
    }
}
