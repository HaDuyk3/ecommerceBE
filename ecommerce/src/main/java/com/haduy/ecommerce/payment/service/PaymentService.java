package com.haduy.ecommerce.payment.service;

import com.haduy.ecommerce.common.enums.OrderStatus;
import com.haduy.ecommerce.common.enums.PaymentProvider;
import com.haduy.ecommerce.common.enums.PaymentStatus;
import com.haduy.ecommerce.common.exception.BusinessException;
import com.haduy.ecommerce.common.exception.ErrorCode;
import com.haduy.ecommerce.order.entity.Order;
import com.haduy.ecommerce.order.repository.OrderRepository;
import com.haduy.ecommerce.payment.dto.*;
import com.haduy.ecommerce.payment.entity.Payment;
import com.haduy.ecommerce.payment.provider.CallbackResult;
import com.haduy.ecommerce.payment.provider.MomoGateway;
import com.haduy.ecommerce.payment.provider.PaymentGateway;
import com.haduy.ecommerce.payment.provider.VnpayGateway;
import com.haduy.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final List<PaymentStatus> ACTIVE_STATUSES =
            List.of(PaymentStatus.PENDING, PaymentStatus.PROCESSING);
    private static final String INFLIGHT_KEY_PREFIX = "payment:inflight:";
    private static final long INFLIGHT_TTL_MINUTES = 15;

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final StringRedisTemplate redisTemplate;
    private final VnpayGateway vnpayGateway;
    private final MomoGateway momoGateway;

    // Map provider → gateway for URL building and status queries
    private Map<PaymentProvider, PaymentGateway> gatewayMap;

    @jakarta.annotation.PostConstruct
    void initGatewayMap() {
        gatewayMap = List.<PaymentGateway>of(vnpayGateway, momoGateway)
                .stream()
                .collect(Collectors.toMap(PaymentGateway::getProvider, Function.identity()));
    }

    // -------------------------------------------------------------------------
    // Init
    // -------------------------------------------------------------------------

    @Transactional
    public PaymentInitResponse createPayment(UUID userId, PaymentInitRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL,
                    "Order is not awaiting payment");
        }

        // COD: confirm immediately, no redirect (T3.2)
        if (request.getProvider() == PaymentProvider.COD) {
            return createCodPayment(order);
        }

        // Resume existing active payment (F5 / new tab)
        Optional<Payment> activePayment = paymentRepository
                .findActiveByOrderId(order.getId(), ACTIVE_STATUSES);
        if (activePayment.isPresent()) {
            Payment p = activePayment.get();
            log.info("Resuming payment {} for order {}", p.getId(), order.getId());
            return buildResponse(p);
        }

        // Acquire Redis in-flight lock
        String lockKey = INFLIGHT_KEY_PREFIX + order.getId();
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", INFLIGHT_TTL_MINUTES, TimeUnit.MINUTES);

        if (locked == null || !locked) {
            return paymentRepository.findActiveByOrderId(order.getId(), ACTIVE_STATUSES)
                    .map(this::buildResponse)
                    .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_IN_PROGRESS));
        }

        PaymentGateway gateway = gatewayMap.get(request.getProvider());
        String providerRef = generateProviderRef(request.getProvider());
        String paymentUrl = gateway.buildPaymentUrl(providerRef, order.getTotalAmount());

        Payment payment = Payment.builder()
                .order(order)
                .provider(request.getProvider())
                .providerRef(providerRef)
                .paymentUrl(paymentUrl)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);
        log.info("Created payment {} for order {}", payment.getId(), order.getId());
        return buildResponse(payment);
    }

    private PaymentInitResponse createCodPayment(Order order) {
        Payment payment = Payment.builder()
                .order(order)
                .provider(PaymentProvider.COD)
                .providerRef(generateProviderRef(PaymentProvider.COD))
                .paymentUrl("")
                .amount(order.getTotalAmount())
                .status(PaymentStatus.SUCCESS)
                .paidAt(Instant.now())
                .build();
        paymentRepository.save(payment);

        order.setStatus(OrderStatus.CONFIRMED);
        order.setPaidAt(Instant.now());
        orderRepository.save(order);

        log.info("COD payment confirmed for order {}", order.getId());
        return buildResponse(payment);
    }

    // -------------------------------------------------------------------------
    // Verified callbacks (T3.1)
    // -------------------------------------------------------------------------

    @Transactional
    public PaymentDto processVnpayCallback(VnpayCallbackRequest request) {
        // Find payment by providerRef to get expected amount before verifying
        Payment payment = paymentRepository.findByProviderRef(request.getVnp_TxnRef())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return PaymentDto.from(payment); // idempotent
        }

        CallbackResult result = vnpayGateway.parseAndVerify(request, payment.getAmount());
        return applyCallbackResult(payment, result);
    }

    @Transactional
    public PaymentDto processMomoCallback(MomoCallbackRequest request) {
        Payment payment = paymentRepository.findByProviderRef(request.getOrderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return PaymentDto.from(payment);
        }

        CallbackResult result = momoGateway.parseAndVerify(request, payment.getAmount());
        return applyCallbackResult(payment, result);
    }

    /** Dev/simulation callback — no signature check. */
    @Transactional
    public PaymentDto verifyCallback(PaymentCallbackRequest request) {
        Payment payment = paymentRepository.findByProviderRef(request.getProviderRef())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return PaymentDto.from(payment);
        }

        CallbackResult result = new CallbackResult(
                request.getProviderRef(),
                "SUCCESS".equalsIgnoreCase(request.getStatus()),
                payment.getAmount()
        );
        return applyCallbackResult(payment, result);
    }

    private PaymentDto applyCallbackResult(Payment payment, CallbackResult result) {
        Order order = orderRepository.findByIdWithLock(payment.getOrder().getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (result.success()) {
            if (order.getStatus() == OrderStatus.CANCELLED) {
                payment.setStatus(PaymentStatus.FAILED);
                log.warn("Order {} cancelled but gateway SUCCESS for payment {}. Manual refund needed.",
                        order.getId(), payment.getId());
            } else {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPaidAt(Instant.now());
                order.setStatus(OrderStatus.PAID);
                order.setPaidAt(Instant.now());
                orderRepository.save(order);
                log.info("Payment success for order {}", order.getId());
            }
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            log.warn("Payment failed for providerRef {}", payment.getProviderRef());
        }

        releaseInflightLock(order.getId());
        return PaymentDto.from(paymentRepository.save(payment));
    }

    // -------------------------------------------------------------------------
    // Helpers used by jobs
    // -------------------------------------------------------------------------

    public void releaseInflightLock(UUID orderId) {
        redisTemplate.delete(INFLIGHT_KEY_PREFIX + orderId);
    }

    public Optional<String> queryGatewayStatus(Payment payment) {
        PaymentGateway gateway = gatewayMap.get(payment.getProvider());
        if (gateway == null) return Optional.empty();
        return gateway.queryStatus(payment.getProviderRef());
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private PaymentInitResponse buildResponse(Payment payment) {
        return PaymentInitResponse.builder()
                .paymentId(payment.getId())
                .paymentUrl(payment.getPaymentUrl())
                .provider(payment.getProvider().name())
                .build();
    }

    private String generateProviderRef(PaymentProvider provider) {
        return provider.name() + "_"
                + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
