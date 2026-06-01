package com.haduy.ecommerce.seller.dto;

import com.haduy.ecommerce.common.enums.SellerStatus;
import com.haduy.ecommerce.seller.entity.Seller;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class SellerDto {
    private UUID id;
    private UUID userId;
    private String shopName;
    private Double rating;
    private Long totalSold;
    private SellerStatus status;

    public static SellerDto from(Seller seller) {
        return SellerDto.builder()
                .id(seller.getId())
                .userId(seller.getUser().getId())
                .shopName(seller.getShopName())
                .rating(seller.getRating())
                .totalSold(seller.getTotalSold())
                .status(seller.getStatus())
                .build();
    }
}