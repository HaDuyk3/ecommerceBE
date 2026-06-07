package com.haduy.ecommerce.common.util;

import java.text.Normalizer;

public final class SearchKeywordUtils {

    private SearchKeywordUtils() {
    }

    public static String normalize(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim().replaceAll("\\s+", " ");
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String removeAccents(String text) {
        if (text == null) {
            return null;
        }
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}+", "");
    }

    /** Lowercase pattern without accents for SQL LIKE. */
    public static String likePattern(String keyword) {
        String normalized = normalize(keyword);
        if (normalized == null) {
            return null;
        }
        return "%" + removeAccents(normalized).toLowerCase() + "%";
    }
}
