package com.haduy.ecommerce.pricing.service;

import com.haduy.ecommerce.offer.entity.SellerProduct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class ShippingService {

    private static final BigDecimal FREE_SHIP_THRESHOLD = BigDecimal.valueOf(500_000);
    private static final BigDecimal FREE_SHIP_DISCOUNT_PERCENT = BigDecimal.valueOf(50);

    public BigDecimal calculateShippingFee(SellerProduct sellerProduct) {
        return sellerProduct.getBaseShippingFee();
    }

    public BigDecimal calculateShippingDiscount(SellerProduct sellerProduct,
                                                BigDecimal subtotal) {
        // Đơn hàng >= 500k được giảm 50% phí ship
        if (subtotal.compareTo(FREE_SHIP_THRESHOLD) >= 0) {
            return sellerProduct.getBaseShippingFee()
                    .multiply(FREE_SHIP_DISCOUNT_PERCENT)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}