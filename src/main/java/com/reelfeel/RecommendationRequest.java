package com.reelfeel;

import java.util.List;

/**
 * Sayfanın gönderdiği istek. genre ve excluded boş olabilir.
 */
public record RecommendationRequest(String mood, String genre, List<String> excluded) {
}
