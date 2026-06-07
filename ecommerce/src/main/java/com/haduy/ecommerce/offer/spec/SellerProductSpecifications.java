package com.haduy.ecommerce.offer.spec;

import com.haduy.ecommerce.offer.dto.SellerProductSearchCriteria;
import com.haduy.ecommerce.offer.entity.SellerProduct;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class SellerProductSpecifications {

    private SellerProductSpecifications() {
    }

    public static Specification<SellerProduct> from(SellerProductSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getSellerId() != null) {
                predicates.add(cb.equal(root.get("seller").get("id"), criteria.getSellerId()));
            }
            if (criteria.getProductId() != null) {
                predicates.add(cb.equal(root.get("product").get("id"), criteria.getProductId()));
            }
            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }
            if (Boolean.TRUE.equals(criteria.getInStock())) {
                predicates.add(cb.greaterThan(root.get("stock"), 0));
            } else if (Boolean.FALSE.equals(criteria.getInStock())) {
                predicates.add(cb.equal(root.get("stock"), 0));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
