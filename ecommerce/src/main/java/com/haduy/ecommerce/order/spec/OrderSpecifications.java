package com.haduy.ecommerce.order.spec;

import com.haduy.ecommerce.common.util.SearchKeywordUtils;
import com.haduy.ecommerce.order.dto.OrderSearchCriteria;
import com.haduy.ecommerce.order.entity.Order;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class OrderSpecifications {

    private OrderSpecifications() {
    }

    public static Specification<Order> from(OrderSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getUserId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), criteria.getUserId()));
            }
            if (criteria.getOrderId() != null) {
                predicates.add(cb.equal(root.get("id"), criteria.getOrderId()));
            }
            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }
            if (criteria.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.getFromDate()));
            }
            if (criteria.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.getToDate()));
            }

            String emailPattern = SearchKeywordUtils.likePattern(criteria.getUserEmail());
            if (emailPattern != null) {
                predicates.add(cb.like(cb.lower(root.get("user").get("email")), emailPattern));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
