package com.haduy.ecommerce.seller.controller;

import com.haduy.ecommerce.common.response.ApiResponse;
import com.haduy.ecommerce.seller.dto.SellerDto;
import com.haduy.ecommerce.seller.dto.SellerRequest;
import com.haduy.ecommerce.seller.service.SellerService;
import com.haduy.ecommerce.user.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<SellerDto>> register(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SellerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        sellerService.register(userDetails.getId(), request)));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<SellerDto>> getMyShop(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerService.getMyShop(userDetails.getId())));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<SellerDto>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SellerRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                sellerService.update(userDetails.getId(), request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SellerDto>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(sellerService.getById(id)));
    }
}