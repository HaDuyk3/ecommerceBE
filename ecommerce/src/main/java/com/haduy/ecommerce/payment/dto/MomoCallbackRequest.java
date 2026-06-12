package com.haduy.ecommerce.payment.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * MoMo IPN callback body (v2 JSON format).
 */
@Getter
@Setter
public class MomoCallbackRequest {
    private String partnerCode;
    private String orderId;        // our providerRef
    private String requestId;
    private Long   amount;
    private String orderInfo;
    private String orderType;
    private Long   transId;
    private Integer resultCode;    // 0 = success
    private String message;
    private String payType;
    private Long   responseTime;
    private String extraData;
    private String signature;
}
