package com.haduy.ecommerce.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.haduy.ecommerce.cart.entity.Cart;
import com.haduy.ecommerce.cart.entity.CartItem;
import com.haduy.ecommerce.cart.repository.CartItemRepository;
import com.haduy.ecommerce.cart.service.CartService;
import com.haduy.ecommerce.common.enums.OrderStatus;
import com.haduy.ecommerce.common.exception.BusinessException;
import com.haduy.ecommerce.common.exception.ErrorCode;
import com.haduy.ecommerce.offer.entity.SellerProduct;
import com.haduy.ecommerce.offer.repository.SellerProductRepository;
import com.haduy.ecommerce.order.dto.CheckoutRequest;
import com.haduy.ecommerce.order.dto.OrderDto;
import com.haduy.ecommerce.order.entity.Order;
import com.haduy.ecommerce.order.entity.OrderItem;
import com.haduy.ecommerce.order.repository.OrderRepository;
import com.haduy.ecommerce.pricing.dto.PricingResult;
import com.haduy.ecommerce.pricing.service.PricingService;
import com.haduy.ecommerce.user.dto.AddressDto;
import com.haduy.ecommerce.user.entity.User;
import com.haduy.ecommerce.user.repository.UserAddressRepository;
import com.haduy.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import com.haduy.ecommerce.order.dto.OrderSearchCriteria;
import com.haduy.ecommerce.order.spec.OrderSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    private final PricingService pricingService;
    private final SellerProductRepository sellerProductRepository;
    private final UserService userService;
    private final UserAddressRepository addressRepository;
    private final ObjectMapper objectMapper;

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

        // Tính giá và tạo order items
        BigDecimal totalSubtotal = BigDecimal.ZERO;
        BigDecimal totalShippingFee = BigDecimal.ZERO;
        BigDecimal totalShippingDiscount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            SellerProduct sp = cartItem.getSellerProduct();

            // Tính giá server-side — không trust client
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
        Order order = findOrThrow(orderId);
        if (!order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_CANCEL);
        }

        for (OrderItem item : order.getItems()) {
            sellerProductRepository.restoreStock(item.getSellerProductId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
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