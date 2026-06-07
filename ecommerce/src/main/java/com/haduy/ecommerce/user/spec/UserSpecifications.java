package com.haduy.ecommerce.user.spec;

import com.haduy.ecommerce.common.util.SearchKeywordUtils;
import com.haduy.ecommerce.user.dto.UserSearchCriteria;
import com.haduy.ecommerce.user.entity.User;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> from(UserSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String emailPattern = SearchKeywordUtils.likePattern(criteria.getEmail());
            if (emailPattern != null) {
                predicates.add(cb.like(cb.lower(root.get("email")), emailPattern));
            }
            if (criteria.getRole() != null) {
                predicates.add(cb.equal(root.get("role"), criteria.getRole()));
            }
            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
