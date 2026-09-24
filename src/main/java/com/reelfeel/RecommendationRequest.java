package com.reelfeel;

import java.util.List;

public record RecommendationRequest(String mood, String genre, List<String> excluded) {
}
