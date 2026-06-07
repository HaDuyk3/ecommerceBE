package com.haduy.ecommerce.user.controller;

import com.haduy.ecommerce.common.enums.Role;
import com.haduy.ecommerce.common.enums.UserStatus;
import com.haduy.ecommerce.common.response.ApiResponse;
import com.haduy.ecommerce.user.dto.UserDto;
import com.haduy.ecommerce.user.dto.UserSearchCriteria;
import com.haduy.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserDto>>> search(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        UserSearchCriteria criteria = UserSearchCriteria.builder()
                .email(email)
                .role(role)
                .status(status)
                .build();
        return ResponseEntity.ok(ApiResponse.success(userService.searchAdmin(criteria, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getById(id)));
    }

    @PatchMapping("/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> ban(@PathVariable UUID id) {
        userService.ban(id);
        return ResponseEntity.ok(ApiResponse.success("Đã khóa tài khoản"));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable UUID id) {
        userService.activate(id);
        return ResponseEntity.ok(ApiResponse.success("Đã kích hoạt tài khoản"));
    }
}
