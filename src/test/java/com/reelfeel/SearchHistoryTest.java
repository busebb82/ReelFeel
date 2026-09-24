package com.reelfeel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchHistoryTest {

    @TempDir
    Path tempDir;

    @Test
    void newestSearchComesFirst() {
        SearchHistory history = new SearchHistory(tempDir.resolve("gecmis.txt"));
        history.add("mutluyum");
        history.add("yorgunum");

        assertEquals(List.of("yorgunum", "mutluyum"), history.getMoods());
    }

    @Test
    void keepsOnlyLastFiveWithoutDuplicates() {
        SearchHistory history = new SearchHistory(tempDir.resolve("gecmis.txt"));
        for (int i = 1; i <= 6; i++) {
            history.add("arama " + i);
        }
        history.add("arama 3");

        List<String> moods = history.getMoods();
        assertEquals(5, moods.size());
        assertEquals("arama 3", moods.get(0));
        assertEquals(1, moods.stream().filter(m -> m.equals("arama 3")).count());
    }

    @Test
    void savedSearchesAreLoadedAgain() {
        Path file = tempDir.resolve("gecmis.txt");
        new SearchHistory(file).add("bugün\nçok güzel bir gün");

        SearchHistory loaded = new SearchHistory(file);

        assertEquals(List.of("bugün çok güzel bir gün"), loaded.getMoods());
    }

    @Test
    void missingFileMeansEmptyHistory() {
        SearchHistory history = new SearchHistory(tempDir.resolve("yok.txt"));

        assertTrue(history.getMoods().isEmpty());
    }
}
