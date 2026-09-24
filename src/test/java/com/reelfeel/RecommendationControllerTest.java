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
    void requestWithoutGenreReachesGemini() {
        // Testlerde GEMINI_API_KEY yok; tür boşken hata vermeden anahtar kontrolüne kadar gelmeli
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> controller.recommend(new RecommendationRequest("mutluyum", null, null)));

        assertTrue(e.getMessage().contains("GEMINI_API_KEY"));
    }

    @Test
    void genresAreListed() {
        assertTrue(controller.genres().contains("Komedi"));
    }
}
