package com.haduy.ecommerce.payment.controller;

import com.haduy.ecommerce.common.response.ApiResponse;
import com.haduy.ecommerce.payment.dto.*;
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

    /** VNPAY IPN — verifies HMAC-SHA512 signature before processing. */
    @PostMapping("/callback/vnpay")
    public ResponseEntity<ApiResponse<PaymentDto>> vnpayCallback(
            @ModelAttribute VnpayCallbackRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.processVnpayCallback(request)));
    }

    /** MoMo IPN — verifies HMAC-SHA256 signature before processing. */
    @PostMapping("/callback/momo")
    public ResponseEntity<ApiResponse<PaymentDto>> momoCallback(
            @Valid @RequestBody MomoCallbackRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.processMomoCallback(request)));
    }

    /**
     * Dev/simulation callback only — no signature verification.
     * Must be disabled or protected in production.
     */
    @PostMapping("/callback/dev")
    public ResponseEntity<ApiResponse<PaymentDto>> devCallback(
            @Valid @RequestBody PaymentCallbackRequest request) {
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
