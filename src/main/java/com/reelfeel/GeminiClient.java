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
        apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "GEMINI_API_KEY ortam değişkeni bulunamadı. README'deki kurulum adımlarına bak.");
        }
    }

    public List<Film> recommend(String mood) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(createRequestBody(mood)))
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

    private String createRequestBody(String mood) {
        String prompt = "Kullanıcının ruh hali: \"" + mood + "\"\n"
                + "Bu ruh haline uygun, gerçekten var olan 5 film öner. "
                + "Her film için orijinal adını, çıkış yılını ve neden uygun olduğunu anlatan "
                + "1-2 cümlelik Türkçe bir açıklama yaz. Kullanıcıya \"sen\" diye hitap et. "
                + "Cevabı sadece şu formatta bir JSON dizisi olarak ver: "
                + "[{\"title\": \"...\", \"year\": 2000, \"reason\": \"...\"}]";

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
