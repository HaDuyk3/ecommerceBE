package com.haduy.ecommerce.order.controller;

import com.haduy.ecommerce.common.enums.OrderStatus;
import com.haduy.ecommerce.common.response.ApiResponse;
import com.haduy.ecommerce.order.dto.CheckoutRequest;
import com.haduy.ecommerce.order.dto.OrderDto;
import com.haduy.ecommerce.order.dto.OrderSearchCriteria;
import com.haduy.ecommerce.order.service.OrderService;
import com.haduy.ecommerce.user.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderDto>> checkout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        orderService.checkout(userDetails.getId(), request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        OrderSearchCriteria criteria = OrderSearchCriteria.builder()
                .status(status)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();
        return ResponseEntity.ok(ApiResponse.success(
                orderService.searchMyOrders(userDetails.getId(), criteria, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto>> getById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.getById(userDetails.getId(), id)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderDto>> cancel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.cancel(userDetails.getId(), id)));
    }
}
