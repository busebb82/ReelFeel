package com.reelfeel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RecommendationControllerTest {

    @Test
    void bosRuhHaliKabulEdilmez() {
        RecommendationController controller = new RecommendationController();

        assertThrows(IllegalArgumentException.class,
                () -> controller.recommend(new RecommendationRequest("   ", null, null)));
    }
}
