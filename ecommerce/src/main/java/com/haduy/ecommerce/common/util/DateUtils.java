package com.haduy.ecommerce.common.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private static final DateTimeFormatter VN_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                    .withZone(ZoneId.of("Asian/Ho_Chi_Minh"));

    public static Instant now() {
        return Instant.now();
    }

    public static boolean isExpired(Instant time) {
        return time != null && time.isBefore(Instant.now());
    }

    public static String formatVN(Instant time) {
        if (time == null) return null;
        return VN_FORMAT.format(time);
    }
}