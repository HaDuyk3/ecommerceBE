package com.haduy.ecommerce.payment.service;

import com.haduy.ecommerce.common.enums.OrderStatus;
import com.haduy.ecommerce.common.enums.PaymentProvider;
import com.haduy.ecommerce.common.enums.PaymentStatus;
import com.haduy.ecommerce.common.exception.BusinessException;
import com.haduy.ecommerce.common.exception.ErrorCode;
import com.haduy.ecommerce.order.entity.Order;
import com.haduy.ecommerce.order.repository.OrderRepository;
import com.haduy.ecommerce.payment.dto.PaymentCallbackRequest;
import com.haduy.ecommerce.payment.dto.PaymentDto;
import com.haduy.ecommerce.payment.dto.PaymentInitRequest;
import com.haduy.ecommerce.payment.dto.PaymentInitResponse;
import com.haduy.ecommerce.payment.entity.Payment;
import com.haduy.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public PaymentInitResponse createPayment(UUID userId, PaymentInitRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // Validate order thuộc về user
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // Validate order đang PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL,
                    "Đơn hàng không ở trạng thái chờ thanh toán");
        }

        // Kiểm tra đã có payment chưa
        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        // Tạo providerRef giả lập — thực tế sẽ gọi API của VNPAY/MOMO
        String providerRef = generateProviderRef(request.getProvider());

        Payment payment = Payment.builder()
                .order(order)
                .provider(request.getProvider())
                .providerRef(providerRef)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);

        // Tạo payment URL — thực tế sẽ là URL redirect sang gateway
        String paymentUrl = buildPaymentUrl(request.getProvider(), providerRef,
                order.getTotalAmount().toString());

        return PaymentInitResponse.builder()
                .paymentId(payment.getId())
                .paymentUrl(paymentUrl)
                .provider(request.getProvider().name())
                .build();
    }

    @Transactional
    public PaymentDto verifyCallback(PaymentCallbackRequest request) {
        Payment payment = paymentRepository.findByProviderRef(request.getProviderRef())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if ("SUCCESS".equalsIgnoreCase(request.getStatus())) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaidAt(Instant.now());

            // Cập nhật Order sang PAID
            Order order = payment.getOrder();
            order.setStatus(OrderStatus.PAID);
            order.setPaidAt(Instant.now());
            orderRepository.save(order);

            log.info("Payment success for order: {}", order.getId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            log.warn("Payment failed for providerRef: {}", request.getProviderRef());
        }

        return PaymentDto.from(paymentRepository.save(payment));
    }

    public PaymentDto getByOrderId(UUID userId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return paymentRepository.findByOrderId(orderId)
                .map(PaymentDto::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    private String generateProviderRef(PaymentProvider provider) {
        return provider.name() + "_" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    private String buildPaymentUrl(PaymentProvider provider, String providerRef, String amount) {
        // Placeholder — thực tế tích hợp SDK của từng gateway
        return switch (provider) {
            case VNPAY -> "https://sandbox.vnpayment.vn/pay?ref=" + providerRef + "&amount=" + amount;
            case MOMO -> "https://test-payment.momo.vn/pay?ref=" + providerRef + "&amount=" + amount;
            case COD -> ""; // COD không cần redirect
        };
    }
}