package com.reelfeel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class TmdbClient {

    private static final String SEARCH_URL = "https://api.themoviedb.org/3/search/movie";
    private static final String IMAGE_URL = "https://image.tmdb.org/t/p/w342";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String apiKey;

    public TmdbClient(String apiKey) {
        this.apiKey = apiKey;
    }

    // TMDB isteğe bağlı: anahtar yoksa null döner, site afişsiz çalışır
    public static TmdbClient fromEnvironment() {
        String apiKey = System.getenv("TMDB_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }
        return new TmdbClient(apiKey.trim());
    }

    public void addDetails(Film film) throws IOException, InterruptedException {
        String body = search(film.getTitle(), film.getYear());
        if (!applySearchResult(film, body)) {
            // Gemini yılı yanlış vermiş olabilir, yılsız bir daha dene
            applySearchResult(film, search(film.getTitle(), 0));
        }
    }

    private String search(String title, int year) throws IOException, InterruptedException {
        String url = SEARCH_URL
                + "?language=tr-TR"
                + "&query=" + URLEncoder.encode(title, StandardCharsets.UTF_8);
        if (year > 0) {
            url += "&year=" + year;
        }

        // TMDB iki çeşit anahtar veriyor: uzun "API Read Access Token" (eyJ ile başlar) başlıkta,
        // kısa "API Key" ise adreste gönderilir. Hangisi kopyalanırsa çalışsın.
        HttpRequest.Builder request = HttpRequest.newBuilder();
        if (apiKey.startsWith("eyJ")) {
            request.header("Authorization", "Bearer " + apiKey);
        } else {
            url += "&api_key=" + apiKey;
        }
        request.uri(URI.create(url));

        HttpResponse<String> response = httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("TMDB hatası (kod: " + response.statusCode() + ")");
        }
        return response.body();
    }

    // Arama sonucundaki ilk filmin bilgilerini Film nesnesine yazar. Sonuç yoksa false döner.
    static boolean applySearchResult(Film film, String responseBody) {
        JsonArray results = JsonParser.parseString(responseBody).getAsJsonObject().getAsJsonArray("results");
        if (results == null || results.isEmpty()) {
            return false;
        }

        JsonObject movie = results.get(0).getAsJsonObject();
        film.setOverview(getString(movie, "overview"));
        String posterPath = getString(movie, "poster_path");
        if (posterPath != null) {
            film.setPosterUrl(IMAGE_URL + posterPath);
        }
        if (movie.has("vote_average")) {
            film.setRating(movie.get("vote_average").getAsDouble());
        }
        return true;
    }

    private static String getString(JsonObject json, String key) {
        JsonElement value = json.get(key);
        if (value == null || value.isJsonNull()) {
            return null;
        }
        return value.getAsString();
    }
}
