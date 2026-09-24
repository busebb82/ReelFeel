package com.reelfeel;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class RecommendationController {

    static final List<String> GENRES = List.of(
            "Komedi", "Dram", "Romantik", "Aksiyon", "Macera",
            "Bilim Kurgu", "Korku", "Gerilim", "Animasyon", "Belgesel");

    private static final int MAX_MOOD_LENGTH = 500;

    private final GeminiClient gemini = new GeminiClient();
    private final TmdbClient tmdb = TmdbClient.fromEnvironment();

    @GetMapping("/api/genres")
    public List<String> genres() {
        return GENRES;
    }

    @PostMapping("/api/recommendations")
    public List<Film> recommend(@RequestBody RecommendationRequest request) throws IOException, InterruptedException {
        String mood = request.mood() == null ? "" : request.mood().trim();
        if (mood.isEmpty()) {
            throw new IllegalArgumentException("Önce ruh halini yazmalısın.");
        }
        if (mood.length() > MAX_MOOD_LENGTH) {
            throw new IllegalArgumentException("Ruh halini en fazla " + MAX_MOOD_LENGTH + " karakterle anlatabilirsin.");
        }
        String genre = request.genre() != null && GENRES.contains(request.genre()) ? request.genre() : null;
        List<String> excluded = request.excluded() == null ? List.of() : request.excluded();

        List<Film> films = gemini.recommend(mood, genre, excluded);

        if (tmdb != null) {
            for (Film film : films) {
                try {
                    tmdb.addDetails(film);
                } catch (IOException e) {
                    System.err.println(film.getTitle() + " için TMDB bilgisi alınamadı: " + e.getMessage());
                }
            }
        }
        return films;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler({IOException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleServiceError(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of("message", e.getMessage()));
    }
}
