package com.haduy.ecommerce.seller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerRequest {
    @NotBlank(message = "Tên shop không được để trống")
    @Size(min = 3, max = 50, message = "Tên shop từ 3-50 ký tự")
    private String shopName;
}