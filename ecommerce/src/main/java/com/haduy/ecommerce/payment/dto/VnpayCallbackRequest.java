package com.haduy.ecommerce.payment.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Flattened form of VNPAY IPN/callback query parameters.
 * Gateway POSTs these as query-string or form params.
 */
@Getter
@Setter
public class VnpayCallbackRequest {
    private String vnp_TmnCode;
    private String vnp_Amount;
    private String vnp_BankCode;
    private String vnp_BankTranNo;
    private String vnp_CardType;
    private String vnp_OrderInfo;
    private String vnp_PayDate;
    private String vnp_ResponseCode;   // "00" = success
    private String vnp_TmnCode2;
    private String vnp_TransactionNo;
    private String vnp_TransactionStatus;
    private String vnp_TxnRef;         // our providerRef
    private String vnp_SecureHash;

    /** Converts to a param map for signature verification (excludes SecureHash). */
    public Map<String, String> toParamMap() {
        java.util.Map<String, String> map = new java.util.TreeMap<>();
        putIfPresent(map, "vnp_TmnCode", vnp_TmnCode);
        putIfPresent(map, "vnp_Amount", vnp_Amount);
        putIfPresent(map, "vnp_BankCode", vnp_BankCode);
        putIfPresent(map, "vnp_BankTranNo", vnp_BankTranNo);
        putIfPresent(map, "vnp_CardType", vnp_CardType);
        putIfPresent(map, "vnp_OrderInfo", vnp_OrderInfo);
        putIfPresent(map, "vnp_PayDate", vnp_PayDate);
        putIfPresent(map, "vnp_ResponseCode", vnp_ResponseCode);
        putIfPresent(map, "vnp_TransactionNo", vnp_TransactionNo);
        putIfPresent(map, "vnp_TransactionStatus", vnp_TransactionStatus);
        putIfPresent(map, "vnp_TxnRef", vnp_TxnRef);
        return map;
    }

    private void putIfPresent(Map<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) map.put(key, value);
    }
}
