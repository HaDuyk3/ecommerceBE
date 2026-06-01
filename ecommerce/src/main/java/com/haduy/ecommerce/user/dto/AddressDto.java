package com.haduy.ecommerce.user.dto;

import com.haduy.ecommerce.user.entity.UserAddress;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class AddressDto {
    private UUID id;
    private String fullName;
    private String phone;
    private String province;
    private String district;
    private String ward;
    private String detail;
    private boolean isDefault;

    public static AddressDto from(UserAddress a) {
        return AddressDto.builder()
                .id(a.getId())
                .fullName(a.getFullName())
                .phone(a.getPhone())
                .province(a.getProvince())
                .district(a.getDistrict())
                .ward(a.getWard())
                .detail(a.getDetail())
                .isDefault(a.isDefault())
                .build();
    }
}