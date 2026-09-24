# ReelFeel

Ruh haline göre film öneren bir Java masaüstü uygulaması.

Nasıl hissettiğini bir cümleyle yazıyorsun ("Bugün biraz yorgunum, kafamı dağıtacak bir şey istiyorum" gibi). Uygulama bu metni Google Gemini'ye gönderiyor ve ruh haline uygun 5 film ile her birinin neden uygun olduğunu gösteriyor. TMDB anahtarı tanımlıysa filmlerin afişi, puanı ve konusu da görünüyor.

Diğer özellikler:

- **Tür filtresi:** Sadece komedi, dram, korku gibi belirli bir türden film istenebilir.
- **Başka Öner:** Aynı ruh hali için, daha önce gösterilenlerden farklı 5 film daha getirir.
- **Son aramalar:** Son 5 arama bilgisayarda saklanır (`~/.reelfeel_gecmis.txt`), listeden seçince tekrar yazmaya gerek kalmaz.

## Kullanılanlar

- Java 21 ve Swing
- Google Gemini API
- TMDB API (isteğe bağlı)
- Gson
- Maven, JUnit 5

## Çalıştırma

1. [Google AI Studio](https://aistudio.google.com/apikey)'dan ücretsiz bir Gemini API anahtarı al.
2. Afişleri de görmek istersen [TMDB](https://www.themoviedb.org/settings/api)'de hesap açıp bir anahtar al. Kısa **API Key** ya da uzun **API Read Access Token**, ikisi de çalışır.
3. Anahtarları ortam değişkeni olarak tanımla:

   macOS / Linux:
   ```bash
   export GEMINI_API_KEY="gemini-anahtarın"
   export TMDB_API_KEY="tmdb-anahtarın"
   ```

   Windows (PowerShell):
   ```powershell
   $env:GEMINI_API_KEY="gemini-anahtarın"
   $env:TMDB_API_KEY="tmdb-anahtarın"
   ```

4. Uygulamayı başlat:
   ```bash
   mvn compile exec:java
   ```

## Testler

```bash
mvn test
```

## Dosyalar

| Dosya | Görevi |
|---|---|
| `Main.java` | Uygulamayı başlatır |
| `MainWindow.java` | Ana pencere, butona basınca önerileri getirir |
| `FilmCard.java` | Bir filmi afişi ve açıklamasıyla gösterir |
| `Film.java` | Film bilgilerini tutar |
| `GeminiClient.java` | Gemini'den film önerilerini alır |
| `TmdbClient.java` | TMDB'den afiş, puan ve konu bilgisini alır |
| `SearchHistory.java` | Son aramaları dosyaya kaydeder ve okur |
