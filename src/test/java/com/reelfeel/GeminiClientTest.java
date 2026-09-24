package com.reelfeel;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeminiClientTest {

    @Test
    void parsesFilmsFromGeminiResponse() throws IOException {
        String response = """
                {"candidates": [{"content": {"parts": [{"text":
                  "[{\\"title\\": \\"Amélie\\", \\"year\\": 2001, \\"reason\\": \\"Neşelendirir.\\"}, {\\"title\\": \\"Paddington 2\\", \\"year\\": 2017, \\"reason\\": \\"Umut verir.\\"}]"
                }]}}]}
                """;

        List<Film> films = GeminiClient.parseFilms(response);

        assertEquals(2, films.size());
        assertEquals("Amélie", films.get(0).getTitle());
        assertEquals(2001, films.get(0).getYear());
        assertEquals("Umut verir.", films.get(1).getReason());
    }

    @Test
    void unexpectedResponseGivesReadableError() {
        IOException e = assertThrows(IOException.class, () -> GeminiClient.parseFilms("<html>hata</html>"));

        assertTrue(e.getMessage().contains("beklenmeyen"));
    }

    @Test
    void rateLimitHasItsOwnMessage() {
        assertTrue(GeminiClient.errorMessage(429).contains("limit"));
    }
}
