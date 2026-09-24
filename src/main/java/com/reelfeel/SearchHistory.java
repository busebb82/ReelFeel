package com.reelfeel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Son yazılan ruh hallerini bir metin dosyasında saklar (her satırda bir arama).
 */
public class SearchHistory {

    private static final int MAX_SIZE = 5;

    private final Path file;
    private final List<String> moods = new ArrayList<>();

    public SearchHistory(Path file) {
        this.file = file;
        load();
    }

    public static SearchHistory inHomeFolder() {
        return new SearchHistory(Path.of(System.getProperty("user.home"), ".reelfeel_gecmis.txt"));
    }

    public List<String> getMoods() {
        return new ArrayList<>(moods);
    }

    public void add(String mood) {
        String line = mood.replace('\n', ' ').trim();
        moods.remove(line); // aynı arama varsa en başa taşınsın
        moods.add(0, line);
        while (moods.size() > MAX_SIZE) {
            moods.remove(moods.size() - 1);
        }
        save();
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(file)) {
                if (!line.isBlank() && moods.size() < MAX_SIZE) {
                    moods.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Arama geçmişi okunamadı: " + e.getMessage());
        }
    }

    private void save() {
        try {
            Files.write(file, moods);
        } catch (IOException e) {
            System.err.println("Arama geçmişi kaydedilemedi: " + e.getMessage());
        }
    }
}
