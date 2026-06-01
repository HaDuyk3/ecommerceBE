package com.haduy.ecommerce.promotion.service;

import com.haduy.ecommerce.common.enums.PromotionType;
import com.haduy.ecommerce.common.exception.BusinessException;
import com.haduy.ecommerce.common.exception.ErrorCode;
import com.haduy.ecommerce.offer.entity.SellerProduct;
import com.haduy.ecommerce.offer.service.SellerProductService;
import com.haduy.ecommerce.promotion.dto.PromotionDto;
import com.haduy.ecommerce.promotion.dto.PromotionRequest;
import com.haduy.ecommerce.promotion.entity.Promotion;
import com.haduy.ecommerce.promotion.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final SellerProductService sellerProductService;

    @Transactional
    public PromotionDto create(UUID userId, PromotionRequest request) {
        SellerProduct sp = sellerProductService.findOrThrow(request.getSellerProductId());

        // Validate seller owns this listing
        if (!sp.getSeller().getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // Validate thời gian
        if (request.getStartAt().isAfter(request.getEndAt())) {
            throw new BusinessException(ErrorCode.INVALID_PROMOTION,
                    "Thời gian bắt đầu phải trước thời gian kết thúc");
        }

        // Validate không overlap
        List<Promotion> overlapping = promotionRepository.findOverlapping(
                sp.getId(), request.getStartAt(), request.getEndAt());
        if (!overlapping.isEmpty()) {
            throw new BusinessException(ErrorCode.PROMOTION_OVERLAP);
        }

        // Tính effectivePrice mới
        BigDecimal effectivePrice = calculateEffectivePrice(
                sp.getBasePrice(), request.getType(), request.getValue());
        sp.setEffectivePrice(effectivePrice);

        Promotion promotion = Promotion.builder()
                .sellerProduct(sp)
                .type(request.getType())
                .value(request.getValue())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .build();

        sellerProductService.updateLowestPrice(sp.getProduct().getId());
        return PromotionDto.from(promotionRepository.save(promotion));
    }

    public List<PromotionDto> getMyPromotions(UUID userId) {
        return promotionRepository.findBySellerProductSellerUserId(userId)
                .stream()
                .map(PromotionDto::from)
                .toList();
    }

    @Transactional
    public void delete(UUID userId, UUID promotionId) {
        Promotion promotion = promotionRepository.findById(promotionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROMOTION_NOT_FOUND));

        if (!promotion.getSellerProduct().getSeller().getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        promotionRepository.delete(promotion);
    }

    public java.util.Optional<Promotion> findActiveBySellerProductId(UUID sellerProductId) {
        return promotionRepository.findActiveBySellerProductId(sellerProductId, Instant.now());
    }

    private BigDecimal calculateEffectivePrice(BigDecimal basePrice,
                                               PromotionType type,
                                               BigDecimal value) {
        return switch (type) {
            case PERCENT -> basePrice.multiply(
                            BigDecimal.ONE.subtract(
                                    value.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                    .setScale(2, RoundingMode.HALF_UP);
            case FIXED -> basePrice.subtract(value)
                    .max(BigDecimal.ZERO)
                    .setScale(2, RoundingMode.HALF_UP);
        };
    }
}