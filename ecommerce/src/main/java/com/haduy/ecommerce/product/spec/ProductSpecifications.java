package com.haduy.ecommerce.product.spec;

import com.haduy.ecommerce.common.enums.ProductStatus;
import com.haduy.ecommerce.common.enums.SellerProductStatus;
import com.haduy.ecommerce.common.util.SearchKeywordUtils;
import com.haduy.ecommerce.offer.entity.SellerProduct;
import com.haduy.ecommerce.product.dto.ProductSearchCriteria;
import com.haduy.ecommerce.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> from(ProductSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }

            if (criteria.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), criteria.getCategoryId()));
            }

            if (criteria.getBrand() != null && !criteria.getBrand().isBlank()) {
                String brandPattern = "%" + criteria.getBrand().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("brand")), brandPattern));
            }

            if (criteria.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("lowestPrice"), criteria.getMinPrice()));
            }
            if (criteria.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("lowestPrice"), criteria.getMaxPrice()));
            }

            if (criteria.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("avgRating"), criteria.getMinRating()));
            }

            if (Boolean.TRUE.equals(criteria.getInStock())) {
                predicates.add(hasActiveStock(root, query, cb));
            }

            String likePattern = SearchKeywordUtils.likePattern(criteria.getKeyword());
            if (likePattern != null) {
                Predicate name = cb.like(cb.lower(root.get("name")), likePattern);
                Predicate brand = cb.like(cb.lower(root.get("brand")), likePattern);
                Predicate description = cb.like(cb.lower(root.get("description")), likePattern);
                predicates.add(cb.or(name, brand, description));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate hasActiveStock(Root<Product> root,
                                          jakarta.persistence.criteria.CriteriaQuery<?> query,
                                          jakarta.persistence.criteria.CriteriaBuilder cb) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<SellerProduct> listing = subquery.from(SellerProduct.class);
        subquery.select(cb.literal(1L));
        subquery.where(
                cb.equal(listing.get("product"), root),
                cb.greaterThan(listing.get("stock"), 0),
                cb.equal(listing.get("status"), SellerProductStatus.ACTIVE)
        );
        return cb.exists(subquery);
    }
}
