package com.haduy.ecommerce.promotion.spec;

import com.haduy.ecommerce.common.enums.PromotionStatus;
import com.haduy.ecommerce.promotion.dto.PromotionSearchCriteria;
import com.haduy.ecommerce.promotion.entity.Promotion;
import com.haduy.ecommerce.promotion.enums.PromotionLifecycle;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class PromotionSpecifications {

    private PromotionSpecifications() {
    }

    public static Specification<Promotion> from(PromotionSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Instant now = Instant.now();

            if (criteria.getSellerUserId() != null) {
                predicates.add(cb.equal(
                        root.get("sellerProduct").get("seller").get("user").get("id"),
                        criteria.getSellerUserId()));
            }
            if (criteria.getSellerProductId() != null) {
                predicates.add(cb.equal(
                        root.get("sellerProduct").get("id"),
                        criteria.getSellerProductId()));
            }

            PromotionLifecycle lifecycle = criteria.getLifecycle();
            if (lifecycle != null) {
                switch (lifecycle) {
                    case ACTIVE -> predicates.add(cb.and(
                            cb.equal(root.get("status"), PromotionStatus.ACTIVE),
                            cb.lessThanOrEqualTo(root.get("startAt"), now),
                            cb.greaterThanOrEqualTo(root.get("endAt"), now)));
                    case SCHEDULED -> predicates.add(cb.and(
                            cb.equal(root.get("status"), PromotionStatus.ACTIVE),
                            cb.greaterThan(root.get("startAt"), now)));
                    case EXPIRED -> predicates.add(cb.or(
                            cb.lessThan(root.get("endAt"), now),
                            cb.equal(root.get("status"), PromotionStatus.EXPIRED),
                            cb.equal(root.get("status"), PromotionStatus.INACTIVE)));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
