package com.haduy.ecommerce.order.controller;

import com.haduy.ecommerce.common.enums.OrderStatus;
import com.haduy.ecommerce.common.response.ApiResponse;
import com.haduy.ecommerce.order.dto.OrderDto;
import com.haduy.ecommerce.order.dto.OrderSearchCriteria;
import com.haduy.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderDto>> updateStatus(
            @PathVariable UUID id,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                orderService.adminUpdateStatus(id, status)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderDto>>> search(
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String userEmail,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        OrderSearchCriteria criteria = OrderSearchCriteria.builder()
                .orderId(orderId)
                .userId(userId)
                .userEmail(userEmail)
                .status(status)
                .fromDate(fromDate)
                .toDate(toDate)
                .build();
        return ResponseEntity.ok(ApiResponse.success(
                orderService.searchAdminOrders(criteria, pageable)));
    }
}
