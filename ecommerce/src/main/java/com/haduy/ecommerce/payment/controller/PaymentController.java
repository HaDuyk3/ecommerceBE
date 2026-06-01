package com.haduy.ecommerce.payment.controller;

import com.haduy.ecommerce.common.response.ApiResponse;
import com.haduy.ecommerce.payment.dto.PaymentCallbackRequest;
import com.haduy.ecommerce.payment.dto.PaymentDto;
import com.haduy.ecommerce.payment.dto.PaymentInitRequest;
import com.haduy.ecommerce.payment.dto.PaymentInitResponse;
import com.haduy.ecommerce.payment.service.PaymentService;
import com.haduy.ecommerce.user.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/init")
    public ResponseEntity<ApiResponse<PaymentInitResponse>> initPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PaymentInitRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.createPayment(userDetails.getId(), request)));
    }

    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<PaymentDto>> callback(
            @Valid @RequestBody PaymentCallbackRequest request) {
        // Endpoint này được gọi từ payment gateway webhook
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.verifyCallback(request)));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<PaymentDto>> getByOrderId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.getByOrderId(userDetails.getId(), orderId)));
    }
}