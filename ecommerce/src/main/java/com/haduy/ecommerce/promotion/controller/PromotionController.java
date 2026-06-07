package com.haduy.ecommerce.promotion.controller;

import com.haduy.ecommerce.common.response.ApiResponse;
import com.haduy.ecommerce.promotion.dto.PromotionDto;
import com.haduy.ecommerce.promotion.dto.PromotionRequest;
import com.haduy.ecommerce.promotion.dto.PromotionSearchCriteria;
import com.haduy.ecommerce.promotion.enums.PromotionLifecycle;
import com.haduy.ecommerce.promotion.service.PromotionService;
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
@RequestMapping("/api/seller/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<PromotionDto>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        promotionService.create(userDetails.getId(), request)));
    }

  /**
   * lifecycle: ACTIVE | SCHEDULED | EXPIRED
   */
    @GetMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Page<PromotionDto>>> search(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) UUID sellerProductId,
            @RequestParam(required = false) PromotionLifecycle lifecycle,
            @PageableDefault(size = 20, sort = "startAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        PromotionSearchCriteria criteria = PromotionSearchCriteria.builder()
                .sellerProductId(sellerProductId)
                .lifecycle(lifecycle)
                .build();
        return ResponseEntity.ok(ApiResponse.success(
                promotionService.searchMyPromotions(userDetails.getId(), criteria, pageable)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        promotionService.delete(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa khuyến mãi"));
    }
}
