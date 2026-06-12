package com.haduy.ecommerce.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haduy.ecommerce.cart.entity.Cart;
import com.haduy.ecommerce.cart.entity.CartItem;
import com.haduy.ecommerce.cart.repository.CartItemRepository;
import com.haduy.ecommerce.cart.service.CartService;
import com.haduy.ecommerce.common.enums.OrderStatus;
import com.haduy.ecommerce.common.enums.PaymentStatus;
import com.haduy.ecommerce.common.exception.BusinessException;
import com.haduy.ecommerce.common.exception.ErrorCode;
import com.haduy.ecommerce.offer.entity.SellerProduct;
import com.haduy.ecommerce.offer.repository.SellerProductRepository;
import com.haduy.ecommerce.order.dto.CheckoutRequest;
import com.haduy.ecommerce.order.dto.OrderDto;
import com.haduy.ecommerce.order.entity.Order;
import com.haduy.ecommerce.order.entity.OrderItem;
import com.haduy.ecommerce.order.repository.OrderRepository;
import com.haduy.ecommerce.payment.repository.PaymentRepository;
import com.haduy.ecommerce.payment.service.PaymentService;
import com.haduy.ecommerce.pricing.dto.PricingResult;
import com.haduy.ecommerce.pricing.service.PricingService;
import com.haduy.ecommerce.user.dto.AddressDto;
import com.haduy.ecommerce.user.entity.User;
import com.haduy.ecommerce.user.repository.UserAddressRepository;
import com.haduy.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.haduy.ecommerce.order.dto.OrderSearchCriteria;
import com.haduy.ecommerce.order.spec.OrderSpecifications;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final List<PaymentStatus> ACTIVE_PAYMENT_STATUSES =
            List.of(PaymentStatus.PENDING, PaymentStatus.PROCESSING);

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    private final PricingService pricingService;
    private final SellerProductRepository sellerProductRepository;
    private final UserService userService;
    private final UserAddressRepository addressRepository;
    private final ObjectMapper objectMapper;
    private final PaymentRepository paymentRepository;
    @Lazy private final PaymentService paymentService;

    @Transactional
    public OrderDto checkout(UUID userId, CheckoutRequest request) {
        User user = userService.findOrThrow(userId);

        // Load address và snapshot
        AddressDto address = addressRepository
                .findByIdAndUserId(request.getAddressId(), userId)
                .map(AddressDto::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        String addressSnapshot = toJson(address);

        // Load cart
        Cart cart = cartService.getOrCreateCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new BusinessException(ErrorCode.CART_NOT_FOUND,
                    "Giỏ hàng đang trống");
        }

        // Block checkout if any item has a price mismatch or is out of stock (T2.2)
        List<String> blockedItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()) {
            SellerProduct sp = cartItem.getSellerProduct();
            if (sp.getStock() < cartItem.getQuantity()) {
                blockedItems.add(sp.getProduct().getName() + " (out of stock)");
            } else if (cartItem.getSnapshotPrice().compareTo(sp.getEffectivePrice()) != 0) {
                blockedItems.add(sp.getProduct().getName() + " (price changed)");
            }
        }
        if (!blockedItems.isEmpty()) {
            throw new BusinessException(ErrorCode.CART_PRICE_CHANGED,
                    "Price changed or out of stock: " + String.join(", ", blockedItems));
        }

        // Calculate prices and build order items
        BigDecimal totalSubtotal = BigDecimal.ZERO;
        BigDecimal totalShippingFee = BigDecimal.ZERO;
        BigDecimal totalShippingDiscount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            SellerProduct sp = cartItem.getSellerProduct();

            PricingResult pricing = pricingService.calculate(
                    sp, cartItem.getQuantity(), user);

            // Kiểm tra stock và deduct
            int updated = sellerProductRepository.updateStockConditional(
                    sp.getId(), cartItem.getQuantity());
            if (updated == 0) {
                throw new BusinessException(ErrorCode.OUT_OF_STOCK,
                        sp.getProduct().getName() + " không đủ hàng");
            }

            totalSubtotal = totalSubtotal.add(pricing.getSubtotal());
            totalShippingFee = totalShippingFee.add(pricing.getShippingFee());
            totalShippingDiscount = totalShippingDiscount.add(pricing.getShippingDiscount());

            orderItems.add(OrderItem.builder()
                    .sellerProductId(sp.getId())
                    .productNameSnapshot(sp.getProduct().getName())
                    .quantity(cartItem.getQuantity())
                    .priceSnapshot(pricing.getSubtotal()
                            .divide(BigDecimal.valueOf(cartItem.getQuantity()),
                                    2, java.math.RoundingMode.HALF_UP))
                    .shippingSnapshot(pricing.getShippingFee())
                    .build());
        }

        BigDecimal totalAmount = totalSubtotal
                .add(totalShippingFee)
                .subtract(totalShippingDiscount);

        // Tạo Order
        Order order = Order.builder()
                .user(user)
                .deliveryAddressSnapshot(addressSnapshot)
                .subtotal(totalSubtotal)
                .shippingFee(totalShippingFee)
                .shippingDiscount(totalShippingDiscount)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();

        // Gắn order vào items
        orderItems.forEach(item -> item.setOrder(order));
        order.getItems().addAll(orderItems);

        Order saved = orderRepository.save(order);

        // Xóa cart sau khi checkout thành công
        cartItemRepository.deleteByCartId(cart.getId());

        return OrderDto.from(saved);
    }

    public Page<OrderDto> searchMyOrders(UUID userId, OrderSearchCriteria criteria, Pageable pageable) {
        OrderSearchCriteria effective = OrderSearchCriteria.builder()
                .userId(userId)
                .status(criteria.getStatus())
                .fromDate(criteria.getFromDate())
                .toDate(criteria.getToDate())
                .build();
        return searchOrders(effective, pageable);
    }

    public Page<OrderDto> searchAdminOrders(OrderSearchCriteria criteria, Pageable pageable) {
        return searchOrders(criteria, pageable);
    }

    private Page<OrderDto> searchOrders(OrderSearchCriteria criteria, Pageable pageable) {
        Specification<Order> spec = OrderSpecifications.from(criteria);
        return orderRepository.findAll(spec, pageable).map(OrderDto::from);
    }

    public OrderDto getById(UUID userId, UUID orderId) {
        Order order = findOrThrow(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return OrderDto.from(order);
    }

    @Transactional
    public OrderDto cancel(UUID userId, UUID orderId) {
        // Pessimistic lock to prevent race with verifyCallback (T1.3)
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL);
        }

        // Block cancel while a payment is in flight (T1.3)
        boolean hasActivePayment = paymentRepository
                .findActiveByOrderId(orderId, ACTIVE_PAYMENT_STATUSES)
                .isPresent();
        if (hasActivePayment) {
            throw new BusinessException(ErrorCode.PAYMENT_IN_PROGRESS,
                    "Cannot cancel: payment is being processed");
        }

        return doCancelOrder(order);
    }

    // Used by OrderPendingTimeoutJob — no userId ownership check
    @Transactional
    public void cancelBySystem(UUID orderId) {
        Order order = orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.PENDING) return;

        boolean hasActivePayment = paymentRepository
                .findActiveByOrderId(orderId, ACTIVE_PAYMENT_STATUSES)
                .isPresent();
        if (hasActivePayment) {
            log.info("Timeout job skipping order {} — payment still active", orderId);
            return;
        }

        doCancelOrder(order);
        log.info("Order {} auto-cancelled by timeout job", orderId);
    }

    private OrderDto doCancelOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            sellerProductRepository.restoreStock(item.getSellerProductId(), item.getQuantity());
        }
        order.setStatus(OrderStatus.CANCELLED);
        paymentService.releaseInflightLock(order.getId());
        return OrderDto.from(orderRepository.save(order));
    }

    // Valid transitions admin is allowed to trigger (T4.1)
    private static final Map<OrderStatus, Set<OrderStatus>> ADMIN_TRANSITIONS = Map.of(
            OrderStatus.PAID,      EnumSet.of(OrderStatus.SHIPPED, OrderStatus.REFUNDED),
            OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.REFUNDED),
            OrderStatus.SHIPPED,   EnumSet.of(OrderStatus.DELIVERED)
    );

    @Transactional
    public OrderDto adminUpdateStatus(UUID orderId, OrderStatus newStatus) {
        Order order = findOrThrow(orderId);
        Set<OrderStatus> allowed = ADMIN_TRANSITIONS.getOrDefault(
                order.getStatus(), EnumSet.noneOf(OrderStatus.class));

        if (!allowed.contains(newStatus)) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL,
                    "Cannot transition from " + order.getStatus() + " to " + newStatus);
        }

        order.setStatus(newStatus);
        log.info("Admin updated order {} status: {} → {}", orderId, order.getStatus(), newStatus);
        return OrderDto.from(orderRepository.save(order));
    }

    public Order findOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }
}