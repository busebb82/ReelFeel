<p align="center"><img src="src/main/resources/static/img/logo.svg" width="80" alt="ReelFeel logosu"></p>

# ReelFeel

Ruh haline göre film öneren bir web sitesi.

Nasıl hissettiğini bir cümleyle yazıyorsun ("Bugün biraz yorgunum, kafamı dağıtacak bir şey istiyorum" gibi). Site bu metni Google Gemini'ye gönderiyor ve ruh haline uygun 5 film ile her birinin neden uygun olduğunu gösteriyor. TMDB anahtarı tanımlıysa filmlerin afişi, puanı ve konusu da görünüyor.

Diğer özellikler:

- **Tür filtresi:** Sadece komedi, dram, korku gibi belirli bir türden film istenebilir.
- **Başka Öner:** Aynı ruh hali için, daha önce gösterilenlerden farklı 5 film daha getirir.
- **Son aramalar:** Son 5 arama tarayıcıda saklanır, tıklayınca tekrar yazmaya gerek kalmaz.
- **Örnek ruh halleri:** Ne yazacağını bilemeyenler için hazır örnekler.
- Telefonda da düzgün görünür.

## Nasıl çalışıyor?

Sunucu Java (Spring Boot) ile yazıldı, sayfa ise düz HTML, CSS ve JavaScript. API anahtarları sadece sunucuda duruyor, tarayıcıya hiç gönderilmiyor.

```
Tarayıcı ──POST /api/recommendations──▶ Spring Boot sunucusu ──▶ Gemini (film seçimi)
                                                            └──▶ TMDB (afiş, puan, konu)
```

## Kullanılanlar

- Java 21, Spring Boot
- HTML, CSS, JavaScript
- Google Gemini API
- TMDB API (isteğe bağlı)
- Gson
- Maven, JUnit 5

## Çalıştırma

1. [Google AI Studio](https://aistudio.google.com/apikey)'dan ücretsiz bir Gemini API anahtarı al.
2. Afişleri de görmek istersen [TMDB](https://www.themoviedb.org/settings/api)'de hesap açıp bir anahtar al. Kısa **API Key** ya da uzun **API Read Access Token**, ikisi de çalışır.
3. Anahtarları ortam değişkeni olarak tanımla. Tırnakların içine kendi anahtarını yapıştır:

   macOS / Linux:
   ```bash
   export GEMINI_API_KEY="BURAYA_GEMINI_ANAHTARINI_YAPISTIR"
   export TMDB_API_KEY="BURAYA_TMDB_ANAHTARINI_YAPISTIR"
   ```

   Windows (PowerShell):
   ```powershell
   $env:GEMINI_API_KEY="BURAYA_GEMINI_ANAHTARINI_YAPISTIR"
   $env:TMDB_API_KEY="BURAYA_TMDB_ANAHTARINI_YAPISTIR"
   ```

4. Sunucuyu başlat ve tarayıcıda **http://localhost:8080** adresini aç:
   ```bash
   mvn spring-boot:run
   ```

## İnternete koyma (Render)

Proje [Render](https://render.com) üzerinde ücretsiz yayınlanabilir. Gerekli ayarlar `Dockerfile` ve `render.yaml` dosyalarında hazır.

1. Render'a GitHub hesabınla giriş yap.
2. **New → Blueprint** seç ve bu repoyu bağla.
3. Sorulduğunda `GEMINI_API_KEY` ve `TMDB_API_KEY` değerlerini gir.
4. **Deploy**'a bas. Birkaç dakika sonra site `https://reelfeel-xxxx.onrender.com` gibi bir adreste açılır.

Not: Ücretsiz planda site 15 dakika kullanılmazsa uyku moduna geçer, sonraki ilk açılış yaklaşık bir dakika sürebilir.

## Testler

```bash
mvn test
```

## Dosyalar

| Dosya | Görevi |
|---|---|
| `ReelFeelApplication.java` | Sunucuyu başlatır |
| `RecommendationController.java` | `/api/genres` ve `/api/recommendations` adreslerini karşılar |
| `GeminiClient.java` | Gemini'den film önerilerini alır |
| `TmdbClient.java` | TMDB'den afiş, puan ve konu bilgisini alır |
| `Film.java`, `RecommendationRequest.java` | Veri sınıfları |
| `static/index.html` | Sayfa |
| `static/css/style.css` | Tasarım |
| `static/js/app.js` | Sayfadaki butonlar, istekler ve film kartları |
| `static/img/logo.svg` | Logo |
