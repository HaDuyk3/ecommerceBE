package com.haduy.ecommerce.product.dto;

import com.haduy.ecommerce.common.enums.ProductStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class ProductSearchCriteria {

    private final String keyword;
    private final UUID categoryId;
    private final String brand;
    private final BigDecimal minPrice;
    private final BigDecimal maxPrice;
    private final Double minRating;
    private final Boolean inStock;
    private final ProductStatus status;
}
