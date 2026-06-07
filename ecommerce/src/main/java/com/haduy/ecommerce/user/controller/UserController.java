package com.haduy.ecommerce.user.controller;

import com.haduy.ecommerce.common.response.ApiResponse;
import com.haduy.ecommerce.user.dto.AddressDto;
import com.haduy.ecommerce.user.dto.AddressRequest;
import com.haduy.ecommerce.user.dto.UserDto;
import com.haduy.ecommerce.user.security.CustomUserDetails;
import com.haduy.ecommerce.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getById(userDetails.getId())));
    }

    @GetMapping("/me/addresses")
    public ResponseEntity<ApiResponse<List<AddressDto>>> getMyAddresses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getAddresses(userDetails.getId())));
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<ApiResponse<AddressDto>> addAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        userService.addAddress(userDetails.getId(), request)));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID addressId) {
        userService.deleteAddress(userDetails.getId(), addressId);
        return ResponseEntity.ok(ApiResponse.success("Đã xóa địa chỉ"));
    }
}
