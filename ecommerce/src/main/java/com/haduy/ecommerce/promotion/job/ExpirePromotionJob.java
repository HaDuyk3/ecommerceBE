package com.haduy.ecommerce.promotion.job;

import com.haduy.ecommerce.common.enums.PromotionStatus;
import com.haduy.ecommerce.offer.service.SellerProductService;
import com.haduy.ecommerce.promotion.entity.Promotion;
import com.haduy.ecommerce.promotion.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpirePromotionJob {

    private final PromotionRepository promotionRepository;
    private final SellerProductService sellerProductService;

    // Runs every 5 minutes
    @Scheduled(fixedDelayString = "${app.promotion.expire-check-interval-ms:300000}")
    @Transactional
    public void expireEndedPromotions() {
        List<Promotion> expired = promotionRepository.findExpiredActive(Instant.now());
        if (expired.isEmpty()) return;

        log.info("ExpirePromotionJob: expiring {} promotions", expired.size());
        for (Promotion promotion : expired) {
            try {
                promotion.setStatus(PromotionStatus.EXPIRED);
                promotionRepository.save(promotion);

                // Restore effectivePrice to basePrice — no active promo anymore (T2.3)
                var sp = promotion.getSellerProduct();
                sellerProductService.updateEffectivePrice(sp.getId(), sp.getBasePrice());

                log.info("Expired promotion {} for sellerProduct {}", promotion.getId(), sp.getId());
            } catch (Exception e) {
                log.error("Failed to expire promotion {}: {}", promotion.getId(), e.getMessage());
            }
        }
    }
}
