package com.haduy.ecommerce.payment.provider;

import com.haduy.ecommerce.common.enums.PaymentProvider;
import com.haduy.ecommerce.common.exception.BusinessException;
import com.haduy.ecommerce.common.exception.ErrorCode;
import com.haduy.ecommerce.payment.dto.MomoCallbackRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@Component
public class MomoGateway implements PaymentGateway {

    @Value("${payment.momo.secret-key:sandbox-secret}")
    private String secretKey;

    @Value("${payment.momo.access-key:sandbox-access}")
    private String accessKey;

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.MOMO;
    }

    @Override
    public String buildPaymentUrl(String providerRef, BigDecimal amount) {
        return "https://test-payment.momo.vn/pay?ref=" + providerRef
                + "&amount=" + amount.toPlainString();
    }

    /** Verifies HMAC-SHA256 signature and parses callback. */
    public CallbackResult parseAndVerify(MomoCallbackRequest req, BigDecimal expectedAmount) {
        String rawHash = "accessKey=" + accessKey
                + "&amount=" + req.getAmount()
                + "&extraData=" + nullSafe(req.getExtraData())
                + "&message=" + nullSafe(req.getMessage())
                + "&orderId=" + req.getOrderId()
                + "&orderInfo=" + nullSafe(req.getOrderInfo())
                + "&orderType=" + nullSafe(req.getOrderType())
                + "&partnerCode=" + req.getPartnerCode()
                + "&payType=" + nullSafe(req.getPayType())
                + "&requestId=" + nullSafe(req.getRequestId())
                + "&responseTime=" + req.getResponseTime()
                + "&resultCode=" + req.getResultCode()
                + "&transId=" + req.getTransId();

        String computed = hmacSha256(secretKey, rawHash);
        if (!computed.equalsIgnoreCase(req.getSignature())) {
            log.warn("MoMo callback signature mismatch for ref {}", req.getOrderId());
            throw new BusinessException(ErrorCode.PAYMENT_SIGNATURE_INVALID);
        }

        BigDecimal callbackAmount = BigDecimal.valueOf(req.getAmount());
        if (callbackAmount.compareTo(expectedAmount) != 0) {
            log.warn("MoMo amount mismatch: expected {} got {}", expectedAmount, callbackAmount);
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        return new CallbackResult(req.getOrderId(), req.getResultCode() == 0, callbackAmount);
    }

    @Override
    public Optional<String> queryStatus(String providerRef) {
        // Real integration: call MoMo query transaction API
        return Optional.empty();
    }

    private String hmacSha256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
