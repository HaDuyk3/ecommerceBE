package com.haduy.ecommerce.payment.provider;

import com.haduy.ecommerce.common.enums.PaymentProvider;
import com.haduy.ecommerce.common.exception.BusinessException;
import com.haduy.ecommerce.common.exception.ErrorCode;
import com.haduy.ecommerce.payment.dto.VnpayCallbackRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class VnpayGateway implements PaymentGateway {

    @Value("${payment.vnpay.hash-secret:sandbox-secret}")
    private String hashSecret;

    @Value("${payment.vnpay.tmn-code:SANDBOX}")
    private String tmnCode;

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.VNPAY;
    }

    @Override
    public String buildPaymentUrl(String providerRef, BigDecimal amount) {
        return "https://sandbox.vnpayment.vn/pay?ref=" + providerRef
                + "&amount=" + amount.toPlainString();
    }

    /** Verifies signature and parses callback — throws on invalid signature or wrong amount. */
    public CallbackResult parseAndVerify(VnpayCallbackRequest request, BigDecimal expectedAmount) {
        Map<String, String> params = request.toParamMap();

        String computed = hmacSha512(hashSecret, buildHashData(params));
        if (!computed.equalsIgnoreCase(request.getVnp_SecureHash())) {
            log.warn("VNPAY callback signature mismatch for ref {}", request.getVnp_TxnRef());
            throw new BusinessException(ErrorCode.PAYMENT_SIGNATURE_INVALID);
        }

        // VNPAY sends amount * 100 (e.g. 50000 VND → "5000000")
        BigDecimal callbackAmount = new BigDecimal(request.getVnp_Amount())
                .divide(BigDecimal.valueOf(100));
        if (callbackAmount.compareTo(expectedAmount) != 0) {
            log.warn("VNPAY amount mismatch: expected {} got {}", expectedAmount, callbackAmount);
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        boolean success = "00".equals(request.getVnp_ResponseCode())
                && "00".equals(request.getVnp_TransactionStatus());

        return new CallbackResult(request.getVnp_TxnRef(), success, callbackAmount);
    }

    @Override
    public Optional<String> queryStatus(String providerRef) {
        // Real integration: call VNPAY query API with providerRef
        // Returning empty triggers EXPIRED fallback in PaymentTimeoutJob
        return Optional.empty();
    }

    private String buildHashData(Map<String, String> params) {
        return params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }

    private String hmacSha512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }
}
