package com.reelfeel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TmdbClientTest {

    @Test
    void usesFirstSearchResult() {
        Film film = new Film("Amélie", 2001, "Neşelendirir.");
        String response = """
                {"results": [
                  {"id": 194, "overview": "Paris'te yaşayan utangaç bir garson...", "poster_path": "/amelie.jpg", "vote_average": 7.9},
                  {"id": 5, "overview": "Başka bir film", "poster_path": "/baska.jpg", "vote_average": 5.0}
                ]}
                """;

        assertEquals(194, TmdbClient.applySearchResult(film, response));
        assertEquals("Paris'te yaşayan utangaç bir garson...", film.getOverview());
        assertEquals("https://image.tmdb.org/t/p/w342/amelie.jpg", film.getPosterUrl());
        assertEquals(7.9, film.getRating());
    }

    @Test
    void emptyResultLeavesFilmUnchanged() {
        Film film = new Film("Olmayan Film", 1999, "Test");

        assertEquals(0, TmdbClient.applySearchResult(film, "{\"results\": []}"));
        assertNull(film.getOverview());
        assertNull(film.getPosterUrl());
    }

    @Test
    void handlesMissingPoster() {
        Film film = new Film("Film", 2020, "Test");

        TmdbClient.applySearchResult(film, "{\"results\": [{\"id\": 1, \"overview\": \"Özet\", \"poster_path\": null}]}");

        assertEquals("Özet", film.getOverview());
        assertNull(film.getPosterUrl());
    }

    @Test
    void englishPosterIsPreferred() {
        String images = """
                {"posters": [
                  {"iso_639_1": "tr", "file_path": "/turkce.jpg"},
                  {"iso_639_1": null, "file_path": "/yazisiz.jpg"},
                  {"iso_639_1": "en", "file_path": "/ingilizce.jpg"}
                ]}
                """;

        assertEquals("/ingilizce.jpg", TmdbClient.chooseEnglishPoster(images));
    }

    @Test
    void textlessPosterIsUsedWhenNoEnglishPoster() {
        String images = "{\"posters\": [{\"iso_639_1\": null, \"file_path\": \"/yazisiz.jpg\"}]}";

        assertEquals("/yazisiz.jpg", TmdbClient.chooseEnglishPoster(images));
    }

    @Test
    void noSuitablePosterReturnsNull() {
        assertNull(TmdbClient.chooseEnglishPoster("{\"posters\": []}"));
    }
}
