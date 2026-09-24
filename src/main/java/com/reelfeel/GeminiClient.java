package com.reelfeel;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

public class GeminiClient {

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String apiKey;

    public GeminiClient() {
        this(System.getenv("GEMINI_API_KEY"));
    }

    GeminiClient(String apiKey) {
        this.apiKey = apiKey == null ? null : apiKey.strip();
    }

    /**
     * @param genre          sadece bu türden film istenir, null ise tür fark etmez
     * @param excludedTitles daha önce gösterilen filmler, tekrar önerilmesin diye
     */
    public List<Film> recommend(String mood, String genre, List<String> excludedTitles)
            throws IOException, InterruptedException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Sunucuda GEMINI_API_KEY tanımlı değil. README'deki kurulum adımlarına bak.");
        }
        // Gerçek anahtarlar sadece harf, rakam, - ve _ içerir
        if (!apiKey.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalStateException("GEMINI_API_KEY geçersiz görünüyor. README'deki örnek yazıyı değil, "
                    + "Google AI Studio'dan aldığın gerçek anahtarı yazmalısın.");
        }
        String prompt = createPrompt(mood, genre, excludedTitles);
        HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(createRequestBody(prompt)))
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IOException("Gemini'ye bağlanılamadı. İnternet bağlantını kontrol et.", e);
        }

        if (response.statusCode() != 200) {
            System.err.println("Gemini hatası: " + response.body());
            throw new IOException(errorMessage(response.statusCode()));
        }
        return parseFilms(response.body());
    }

    static String createPrompt(String mood, String genre, List<String> excludedTitles) {
        String prompt = "Kullanıcının ruh hali: \"" + mood + "\"\n"
                + "Bu ruh haline uygun, gerçekten var olan 5 film öner. ";
        if (genre != null) {
            prompt += "Sadece " + genre + " türündeki filmlerden seç. ";
        }
        if (!excludedTitles.isEmpty()) {
            prompt += "Şu filmleri tekrar önerme: " + String.join(", ", excludedTitles) + ". ";
        }
        prompt += "Her film için orijinal adını, çıkış yılını ve neden uygun olduğunu anlatan "
                + "1-2 cümlelik Türkçe bir açıklama yaz. Kullanıcıya \"sen\" diye hitap et. "
                + "Cevabı sadece şu formatta bir JSON dizisi olarak ver: "
                + "[{\"title\": \"...\", \"year\": 2000, \"reason\": \"...\"}]";
        return prompt;
    }

    private String createRequestBody(String prompt) {
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        JsonArray parts = new JsonArray();
        parts.add(part);
        JsonObject content = new JsonObject();
        content.add("parts", parts);
        JsonArray contents = new JsonArray();
        contents.add(content);

        // Gemini'nin düz metin yerine JSON döndürmesini istiyoruz
        JsonObject config = new JsonObject();
        config.addProperty("responseMimeType", "application/json");

        JsonObject body = new JsonObject();
        body.add("contents", contents);
        body.add("generationConfig", config);
        return body.toString();
    }

    // Cevaptaki film listesi candidates[0].content.parts[0].text içinde JSON metni olarak gelir
    static List<Film> parseFilms(String responseBody) throws IOException {
        try {
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();
            String text = json.getAsJsonArray("candidates").get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts").get(0).getAsJsonObject()
                    .get("text").getAsString();

            Film[] films = new Gson().fromJson(text, Film[].class);
            return Arrays.asList(films);
        } catch (RuntimeException e) {
            System.err.println("Beklenmeyen Gemini cevabı: " + responseBody);
            throw new IOException("Gemini'den beklenmeyen bir cevap geldi. Tekrar dene.", e);
        }
    }

    static String errorMessage(int statusCode) {
        return switch (statusCode) {
            case 400, 403 -> "API anahtarı geçersiz olabilir. GEMINI_API_KEY değerini kontrol et.";
            case 429 -> "Gemini'nin ücretsiz kullanım limitine ulaşıldı. Biraz bekleyip tekrar dene.";
            case 500, 503 -> "Gemini şu an çok yoğun. Biraz sonra tekrar dene.";
            default -> "Gemini bir hata döndürdü (kod: " + statusCode + ").";
        };
    }
}
