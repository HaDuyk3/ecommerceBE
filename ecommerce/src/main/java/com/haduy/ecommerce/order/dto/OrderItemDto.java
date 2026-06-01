package com.haduy.ecommerce.order.dto;

import com.haduy.ecommerce.order.entity.OrderItem;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class OrderItemDto {
    private UUID id;
    private UUID sellerProductId;
    private String productNameSnapshot;
    private Integer quantity;
    private BigDecimal priceSnapshot;
    private BigDecimal shippingSnapshot;
    private BigDecimal itemTotal;

    public static OrderItemDto from(OrderItem item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .sellerProductId(item.getSellerProductId())
                .productNameSnapshot(item.getProductNameSnapshot())
                .quantity(item.getQuantity())
                .priceSnapshot(item.getPriceSnapshot())
                .shippingSnapshot(item.getShippingSnapshot())
                .itemTotal(item.getPriceSnapshot()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .build();
    }
}