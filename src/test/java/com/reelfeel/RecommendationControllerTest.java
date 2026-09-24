package com.reelfeel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationControllerTest {

    private final RecommendationController controller = new RecommendationController();

    @Test
    void emptyMoodIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.recommend(new RecommendationRequest("   ", null, null)));
    }

    @Test
    void tooLongMoodIsRejected() {
        String longMood = "a".repeat(501);

        assertThrows(IllegalArgumentException.class,
                () -> controller.recommend(new RecommendationRequest(longMood, null, null)));
    }

    @Test
    void genresAreListed() {
        assertTrue(controller.genres().contains("Komedi"));
    }
}
