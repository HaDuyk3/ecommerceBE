package com.haduy.ecommerce.user.dto;

import com.haduy.ecommerce.common.enums.Role;
import com.haduy.ecommerce.common.enums.UserStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSearchCriteria {

    private final String email;
    private final Role role;
    private final UserStatus status;
}
