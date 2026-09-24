package com.reelfeel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TmdbClientTest {

    @Test
    void usesFirstSearchResult() {
        Film film = new Film("Amélie", 2001, "Neşelendirir.");
        String response = """
                {"results": [
                  {"overview": "Paris'te yaşayan utangaç bir garson...", "poster_path": "/amelie.jpg", "vote_average": 7.9},
                  {"overview": "Başka bir film", "poster_path": "/baska.jpg", "vote_average": 5.0}
                ]}
                """;

        assertTrue(TmdbClient.applySearchResult(film, response));
        assertEquals("Paris'te yaşayan utangaç bir garson...", film.getOverview());
        assertEquals("/amelie.jpg", film.getPosterPath());
        assertEquals(7.9, film.getRating());
    }

    @Test
    void emptyResultLeavesFilmUnchanged() {
        Film film = new Film("Olmayan Film", 1999, "Test");

        assertFalse(TmdbClient.applySearchResult(film, "{\"results\": []}"));
        assertNull(film.getOverview());
        assertNull(film.getPosterPath());
    }

    @Test
    void handlesMissingPoster() {
        Film film = new Film("Film", 2020, "Test");

        TmdbClient.applySearchResult(film, "{\"results\": [{\"overview\": \"Özet\", \"poster_path\": null}]}");

        assertEquals("Özet", film.getOverview());
        assertNull(film.getPosterPath());
    }
}
