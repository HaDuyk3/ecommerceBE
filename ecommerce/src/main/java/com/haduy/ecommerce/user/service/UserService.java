package com.haduy.ecommerce.user.service;

import com.haduy.ecommerce.common.exception.BusinessException;
import com.haduy.ecommerce.common.exception.ErrorCode;
import com.haduy.ecommerce.common.enums.UserStatus;
import com.haduy.ecommerce.user.dto.AddressDto;
import com.haduy.ecommerce.user.dto.AddressRequest;
import com.haduy.ecommerce.user.dto.UserDto;
import com.haduy.ecommerce.user.entity.User;
import com.haduy.ecommerce.user.entity.UserAddress;
import com.haduy.ecommerce.user.repository.UserAddressRepository;
import com.haduy.ecommerce.user.repository.UserRepository;
import com.haduy.ecommerce.user.dto.UserSearchCriteria;
import com.haduy.ecommerce.user.spec.UserSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    private final UserRepository userRepository;
    private final UserAddressRepository addressRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    public UserDto getById(UUID id) {
        return UserDto.from(findOrThrow(id));
    }

    public Page<UserDto> searchAdmin(UserSearchCriteria criteria, Pageable pageable) {
        Specification<User> spec = UserSpecifications.from(criteria);
        return userRepository.findAll(spec, pageable).map(UserDto::from);
    }

    @Transactional
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        User user = findOrThrow(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Mật khẩu cũ không đúng");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void ban(UUID userId) {
        User user = findOrThrow(userId);
        user.setStatus(UserStatus.BANNED);
        userRepository.save(user);
        // Invalidate any existing refresh token so the ban takes effect immediately (T4.3)
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + user.getEmail());
    }

    @Transactional
    public void activate(UUID userId) {
        User user = findOrThrow(userId);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    public List<AddressDto> getAddresses(UUID userId) {
        return addressRepository.findByUserId(userId).stream()
                .map(AddressDto::from)
                .toList();
    }

    @Transactional
    public AddressDto addAddress(UUID userId, AddressRequest request) {
        User user = findOrThrow(userId);
        if (request.isDefault()) {
            clearDefaultAddresses(userId);
        }
        UserAddress address = UserAddress.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .province(request.getProvince())
                .district(request.getDistrict())
                .ward(request.getWard())
                .detail(request.getDetail())
                .isDefault(request.isDefault())
                .build();
        return AddressDto.from(addressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(UUID userId, UUID addressId) {
        UserAddress address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));
        addressRepository.delete(address);
    }

    public User findOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void clearDefaultAddresses(UUID userId) {
        addressRepository.findByUserId(userId).forEach(a -> {
            if (a.isDefault()) {
                a.setDefault(false);
                addressRepository.save(a);
            }
        });
    }
}