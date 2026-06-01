package com.haduy.ecommerce.user.dto;

import com.haduy.ecommerce.common.enums.Role;
import com.haduy.ecommerce.common.enums.UserStatus;
import com.haduy.ecommerce.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class UserDto {
    private UUID id;
    private String email;
    private Role role;
    private UserStatus status;
    private Instant createdAt;

    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}