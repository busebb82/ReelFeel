package com.reelfeel;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeminiClientTest {

    @Test
    void filmleriOkur() throws IOException {
        String cevap = """
                {"candidates": [{"content": {"parts": [{"text":
                  "[{\\"title\\": \\"Amélie\\", \\"year\\": 2001, \\"reason\\": \\"Neşelendirir.\\"}, {\\"title\\": \\"Paddington 2\\", \\"year\\": 2017, \\"reason\\": \\"Umut verir.\\"}]"
                }]}}]}
                """;

        List<Film> filmler = GeminiClient.parseFilms(cevap);

        assertEquals(2, filmler.size());
        assertEquals("Amélie", filmler.get(0).getTitle());
        assertEquals(2017, filmler.get(1).getYear());
    }

    @Test
    void bozukCevaptaHataVerir() {
        assertThrows(IOException.class, () -> GeminiClient.parseFilms("<html>hata</html>"));
    }

    @Test
    void promptaTurVeOncekiFilmlerEklenir() {
        String prompt = GeminiClient.createPrompt("mutluyum", "Komedi", List.of("Amélie", "Paddington 2"));

        assertTrue(prompt.contains("Komedi"));
        assertTrue(prompt.contains("Amélie, Paddington 2"));
    }

    @Test
    void limitHatasininKendiMesajiVar() {
        assertTrue(GeminiClient.errorMessage(429).contains("limit"));
    }
}
