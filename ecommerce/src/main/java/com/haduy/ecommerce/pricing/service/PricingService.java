package com.haduy.ecommerce.pricing.service;

import com.haduy.ecommerce.common.exception.BusinessException;
import com.haduy.ecommerce.common.exception.ErrorCode;
import com.haduy.ecommerce.offer.entity.SellerProduct;
import com.haduy.ecommerce.pricing.dto.PricingResult;
import com.haduy.ecommerce.promotion.entity.Promotion;
import com.haduy.ecommerce.promotion.service.PromotionService;
import com.haduy.ecommerce.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final PromotionService promotionService;
    private final ShippingService shippingService;

    public PricingResult calculate(SellerProduct sellerProduct, int quantity, User user) {
        // Kiểm tra stock
        if (sellerProduct.getStock() < quantity) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK);
        }

        // Tính effectivePrice — apply promotion nếu có
        BigDecimal effectivePrice = calculateEffectivePrice(sellerProduct);

        // Tính subtotal
        BigDecimal subtotal = effectivePrice
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);

        // Tính shipping
        BigDecimal shippingFee = shippingService.calculateShippingFee(sellerProduct);
        BigDecimal shippingDiscount = shippingService.calculateShippingDiscount(
                sellerProduct, subtotal);
        BigDecimal finalShipping = shippingFee.subtract(shippingDiscount)
                .max(BigDecimal.ZERO);

        // Tính total
        BigDecimal totalAmount = subtotal.add(finalShipping)
                .setScale(2, RoundingMode.HALF_UP);

        return PricingResult.builder()
                .subtotal(subtotal)
                .shippingFee(shippingFee)
                .shippingDiscount(shippingDiscount)
                .totalAmount(totalAmount)
                .build();
    }

    private BigDecimal calculateEffectivePrice(SellerProduct sellerProduct) {
        Optional<Promotion> activePromotion = promotionService
                .findActiveBySellerProductId(sellerProduct.getId());

        if (activePromotion.isEmpty()) {
            return sellerProduct.getBasePrice();
        }

        Promotion promotion = activePromotion.get();
        return switch (promotion.getType()) {
            case PERCENT -> sellerProduct.getBasePrice()
                    .multiply(BigDecimal.ONE.subtract(
                            promotion.getValue().divide(
                                    BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                    .setScale(2, RoundingMode.HALF_UP);
            case FIXED -> sellerProduct.getBasePrice()
                    .subtract(promotion.getValue())
                    .max(BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);
        };
    }
}