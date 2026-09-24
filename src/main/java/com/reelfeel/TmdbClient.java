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

    private static final String API_URL = "https://api.themoviedb.org/3";
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
        apiKey = GeminiClient.cleanKey(apiKey);
        if (!apiKey.matches("[\\x21-\\x7E]+")) {
            System.err.println("TMDB_API_KEY geçersiz görünüyor, afişler gösterilmeyecek. "
                    + "TMDB'den aldığın gerçek anahtarı yazmalısın.");
            return null;
        }
        return new TmdbClient(apiKey);
    }

    public void addDetails(Film film) throws IOException, InterruptedException {
        int id = applySearchResult(film, search(film.getTitle(), film.getYear()));
        if (id == 0) {
            // Gemini yılı yanlış vermiş olabilir, yılsız bir daha dene
            id = applySearchResult(film, search(film.getTitle(), 0));
        }
        if (id == 0) {
            return;
        }

        // Arama Türkçe yapıldığı için afiş de Türkçe gelebiliyor; orijinal (İngilizce) afişi ayrıca istiyoruz
        String englishPoster = chooseEnglishPoster(get("/movie/" + id + "/images", "include_image_language=en,null"));
        if (englishPoster != null) {
            film.setPosterUrl(IMAGE_URL + englishPoster);
        }
    }

    private String search(String title, int year) throws IOException, InterruptedException {
        String query = "language=tr-TR&query=" + URLEncoder.encode(title, StandardCharsets.UTF_8);
        if (year > 0) {
            query += "&year=" + year;
        }
        return get("/search/movie", query);
    }

    private String get(String path, String query) throws IOException, InterruptedException {
        String url = API_URL + path + "?" + query;

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

    // Arama sonucundaki ilk filmin bilgilerini Film nesnesine yazar ve filmin TMDB numarasını döner.
    // Sonuç yoksa 0 döner.
    static int applySearchResult(Film film, String responseBody) {
        JsonArray results = JsonParser.parseString(responseBody).getAsJsonObject().getAsJsonArray("results");
        if (results == null || results.isEmpty()) {
            return 0;
        }

        JsonObject movie = results.get(0).getAsJsonObject();
        film.setOverview(getString(movie, "overview"));
        String posterPath = getString(movie, "poster_path"); // İngilizce afiş bulunamazsa bu kullanılır
        if (posterPath != null) {
            film.setPosterUrl(IMAGE_URL + posterPath);
        }
        if (movie.has("vote_average")) {
            film.setRating(movie.get("vote_average").getAsDouble());
        }
        return movie.get("id").getAsInt();
    }

    // Önce İngilizce afişi, yoksa üzerinde yazı olmayan afişi seçer. İkisi de yoksa null döner.
    static String chooseEnglishPoster(String imagesBody) {
        JsonArray posters = JsonParser.parseString(imagesBody).getAsJsonObject().getAsJsonArray("posters");
        if (posters == null) {
            return null;
        }
        String textless = null;
        for (JsonElement element : posters) {
            JsonObject poster = element.getAsJsonObject();
            String language = getString(poster, "iso_639_1");
            if ("en".equals(language)) {
                return getString(poster, "file_path");
            }
            if (language == null && textless == null) {
                textless = getString(poster, "file_path");
            }
        }
        return textless;
    }

    private static String getString(JsonObject json, String key) {
        JsonElement value = json.get(key);
        if (value == null || value.isJsonNull()) {
            return null;
        }
        return value.getAsString();
    }
}
