package com.haduy.ecommerce.product.dto;

import com.haduy.ecommerce.common.enums.ProductStatus;
import com.haduy.ecommerce.product.entity.Product;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class ProductDto {
    private UUID id;
    private String name;
    private String brand;
    private String description;
    private UUID categoryId;
    private String categoryName;
    private ProductStatus status;
    private BigDecimal lowestPrice;
    private Double avgRating;
    private Integer reviewCount;
    private Long totalSold;

    public static ProductDto from(Product p) {
        return ProductDto.builder()
                .id(p.getId())
                .name(p.getName())
                .brand(p.getBrand())
                .description(p.getDescription())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .status(p.getStatus())
                .lowestPrice(p.getLowestPrice())
                .avgRating(p.getAvgRating())
                .reviewCount(p.getReviewCount())
                .totalSold(p.getTotalSold())
                .build();
    }
}