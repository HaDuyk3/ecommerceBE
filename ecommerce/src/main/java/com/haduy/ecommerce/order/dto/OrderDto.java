package com.haduy.ecommerce.order.dto;

import com.haduy.ecommerce.common.enums.OrderStatus;
import com.haduy.ecommerce.order.entity.Order;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class OrderDto {
    private UUID id;
    private String deliveryAddressSnapshot;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal shippingDiscount;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private Instant createdAt;
    private Instant paidAt;
    private List<OrderItemDto> items;

    public static OrderDto from(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .deliveryAddressSnapshot(order.getDeliveryAddressSnapshot())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .shippingDiscount(order.getShippingDiscount())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .paidAt(order.getPaidAt())
                .items(order.getItems().stream()
                        .map(OrderItemDto::from)
                        .toList())
                .build();
    }
}