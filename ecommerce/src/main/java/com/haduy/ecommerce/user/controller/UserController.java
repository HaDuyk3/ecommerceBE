package com.haduy.ecommerce.user.controller;

import com.haduy.ecommerce.user.security.CustomUserDetails;
import com.haduy.ecommerce.common.response.ApiResponse;
import com.haduy.ecommerce.user.dto.AddressDto;
import com.haduy.ecommerce.user.dto.AddressRequest;
import com.haduy.ecommerce.user.dto.UserDto;
import com.haduy.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/users/me")
    public ResponseEntity<ApiResponse<UserDto>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getById(userDetails.getId())));
    }

    @GetMapping("/api/users/me/addresses")
    public ResponseEntity<ApiResponse<List<AddressDto>>> getMyAddresses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getAddresses(userDetails.getId())));
    }

    @PostMapping("/api/users/me/addresses")
    public ResponseEntity<ApiResponse<AddressDto>> addAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        userService.addAddress(userDetails.getId(), request)));
    }

    @DeleteMapping("/api/users/me/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID addressId) {
        userService.deleteAddress(userDetails.getId(), addressId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa địa chỉ"));
    }

    @GetMapping("/api/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getById(id)));
    }

    @PatchMapping("/api/admin/users/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> ban(@PathVariable UUID id) {
        userService.ban(id);
        return ResponseEntity.ok(ApiResponse.success("Đã khóa tài khoản"));
    }

    @PatchMapping("/api/admin/users/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable UUID id) {
        userService.activate(id);
        return ResponseEntity.ok(ApiResponse.success("Đã kích hoạt tài khoản"));
    }
}