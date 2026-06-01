package com.haduy.ecommerce.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Auth
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token đã hết hạn"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Token không hợp lệ"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Chưa xác thực"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Không có quyền truy cập"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Refresh token không tồn tại hoặc đã hết hạn"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email đã tồn tại"),
    USER_BANNED(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa"),

    // Seller
    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy seller"),
    SELLER_ALREADY_EXISTS(HttpStatus.CONFLICT, "Shop đã tồn tại"),

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"),
    PRODUCT_NOT_APPROVED(HttpStatus.BAD_REQUEST, "Sản phẩm chưa được duyệt"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"),
    // Offer
    SELLER_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy listing"),

    // Promotion
    INVALID_PROMOTION(HttpStatus.BAD_REQUEST, "Khuyến mãi không hợp lệ"),
    PROMOTION_OVERLAP(HttpStatus.CONFLICT, "Thời gian khuyến mãi bị trùng"),
    PROMOTION_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy khuyến mãi"),

    // Cart
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy giỏ hàng"),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm trong giỏ"),

    // Order
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"),
    ORDER_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "Không thể hủy đơn hàng này"),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "Sản phẩm không đủ hàng"),

    // Payment
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy thanh toán"),
    PAYMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "Đơn hàng đã có thanh toán"),

    // Review
    DUPLICATE_REVIEW(HttpStatus.CONFLICT, "Sản phẩm này đã được đánh giá"),
    REVIEW_NOT_ALLOWED(HttpStatus.FORBIDDEN, "Chỉ có thể đánh giá sản phẩm đã mua"),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy đánh giá"),

    // Address
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy địa chỉ"),

    // Generic
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Dữ liệu không hợp lệ");

    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}