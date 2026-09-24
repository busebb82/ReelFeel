package com.reelfeel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TmdbClientTest {

    @Test
    void ilkSonucuKullanir() {
        Film film = new Film("Amélie", 2001, "Neşelendirir.");
        String cevap = """
                {"results": [
                  {"id": 194, "overview": "Paris'te bir garson...", "poster_path": "/amelie.jpg", "vote_average": 7.9},
                  {"id": 5, "overview": "Başka bir film", "poster_path": "/baska.jpg", "vote_average": 5.0}
                ]}
                """;

        assertEquals(194, TmdbClient.applySearchResult(film, cevap));
        assertEquals("Paris'te bir garson...", film.getOverview());
        assertEquals(7.9, film.getRating());
    }

    @Test
    void sonucYoksaSifirDoner() {
        Film film = new Film("Olmayan Film", 1999, "Test");

        assertEquals(0, TmdbClient.applySearchResult(film, "{\"results\": []}"));
    }

    @Test
    void ingilizceAfisSecilir() {
        String afisler = """
                {"posters": [
                  {"iso_639_1": "tr", "file_path": "/turkce.jpg"},
                  {"iso_639_1": "en", "file_path": "/ingilizce.jpg"}
                ]}
                """;

        assertEquals("/ingilizce.jpg", TmdbClient.chooseEnglishPoster(afisler));
    }
}
