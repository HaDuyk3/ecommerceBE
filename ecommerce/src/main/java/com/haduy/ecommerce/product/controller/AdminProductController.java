package com.haduy.ecommerce.product.controller;

import com.haduy.ecommerce.common.enums.ProductStatus;
import com.haduy.ecommerce.common.response.ApiResponse;
import com.haduy.ecommerce.product.dto.ProductDto;
import com.haduy.ecommerce.product.dto.ProductSearchCriteria;
import com.haduy.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) ProductStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .status(status)
                .build();
        return ResponseEntity.ok(ApiResponse.success(productService.searchAdmin(criteria, pageable)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDto>> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(productService.approve(id)));
    }
}
