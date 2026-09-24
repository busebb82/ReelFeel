package com.reelfeel;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame {

    private static final String[] GENRES = {
            "Hepsi", "Komedi", "Dram", "Romantik", "Aksiyon", "Macera",
            "Bilim Kurgu", "Korku", "Gerilim", "Animasyon", "Belgesel"
    };

    private final JTextArea moodInput = new JTextArea(3, 40);
    private final JComboBox<String> genreBox = new JComboBox<>(GENRES);
    private final JComboBox<String> historyBox = new JComboBox<>();
    private final JButton recommendButton = new JButton("Film Öner");
    private final JButton moreButton = new JButton("Başka Öner");
    private final JLabel statusLabel = new JLabel(" ");
    private final JPanel resultsPanel = new JPanel();

    // "Başka Öner" denince aynı filmler tekrar gelmesin diye gösterilenleri tutuyoruz
    private final List<String> shownTitles = new ArrayList<>();
    private final SearchHistory history = SearchHistory.inHomeFolder();

    public MainWindow() {
        super("ReelFeel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 750);
        setLocationRelativeTo(null);

        JLabel title = new JLabel("Bugün nasıl hissediyorsun?");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        moodInput.setLineWrap(true);
        moodInput.setWrapStyleWord(true);

        // Uzun aramalar kutuyu çok genişletmesin
        historyBox.setPrototypeDisplayValue("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        updateHistoryBox();
        historyBox.addActionListener(e -> {
            // İlk seçenek sadece başlık, diğerleri seçilince yazı kutusuna gelir
            if (historyBox.getSelectedIndex() > 0) {
                moodInput.setText((String) historyBox.getSelectedItem());
            }
        });

        recommendButton.addActionListener(e -> {
            shownTitles.clear();
            if (!moodInput.getText().isBlank()) {
                history.add(moodInput.getText());
                updateHistoryBox();
            }
            recommend();
        });
        moreButton.addActionListener(e -> recommend());
        moreButton.setEnabled(false);

        JPanel optionsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        optionsRow.add(new JLabel("Tür: "));
        optionsRow.add(genreBox);
        optionsRow.add(new JLabel("    Son aramalar: "));
        optionsRow.add(historyBox);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(moreButton);
        buttons.add(recommendButton);

        JPanel buttonRow = new JPanel(new BorderLayout());
        buttonRow.add(statusLabel, BorderLayout.CENTER);
        buttonRow.add(buttons, BorderLayout.EAST);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        optionsRow.setAlignmentX(LEFT_ALIGNMENT);
        buttonRow.setAlignmentX(LEFT_ALIGNMENT);
        bottom.add(optionsRow);
        bottom.add(buttonRow);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        top.add(title, BorderLayout.NORTH);
        top.add(new JScrollPane(moodInput), BorderLayout.CENTER);
        top.add(bottom, BorderLayout.SOUTH);

        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 16, 16));
        JScrollPane resultsScroll = new JScrollPane(resultsPanel);
        resultsScroll.setBorder(null);
        resultsScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        resultsScroll.getVerticalScrollBar().setUnitIncrement(16);

        add(top, BorderLayout.NORTH);
        add(resultsScroll, BorderLayout.CENTER);
    }

    private void recommend() {
        String mood = moodInput.getText().trim();
        if (mood.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Önce ruh halini yazmalısın.");
            return;
        }
        // İlk seçenek "Hepsi", o zaman tür filtresi yok
        String genre = genreBox.getSelectedIndex() == 0 ? null : (String) genreBox.getSelectedItem();
        List<String> excluded = new ArrayList<>(shownTitles);

        recommendButton.setEnabled(false);
        moreButton.setEnabled(false);
        statusLabel.setText("Filmler aranıyor...");
        showFilms(List.of());

        // İstekler birkaç saniye sürebilir, arayüz donmasın diye arka planda çalıştırıyoruz
        new SwingWorker<List<Film>, Void>() {
            @Override
            protected List<Film> doInBackground() throws Exception {
                List<Film> films = new GeminiClient().recommend(mood, genre, excluded);

                TmdbClient tmdb = TmdbClient.fromEnvironment();
                if (tmdb != null) {
                    for (Film film : films) {
                        try {
                            tmdb.addDetails(film);
                        } catch (IOException e) {
                            // TMDB'de sorun olursa film yine gösterilir, sadece afişsiz olur
                            System.err.println(film.getTitle() + " için TMDB bilgisi alınamadı: " + e.getMessage());
                        }
                    }
                }
                return films;
            }

            @Override
            protected void done() {
                recommendButton.setEnabled(true);
                statusLabel.setText(" ");
                try {
                    List<Film> films = get();
                    showFilms(films);
                    for (Film film : films) {
                        shownTitles.add(film.getTitle());
                    }
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(MainWindow.this, cause.getMessage(),
                            "Hata", JOptionPane.ERROR_MESSAGE);
                }
                moreButton.setEnabled(!shownTitles.isEmpty());
            }
        }.execute();
    }

    private void updateHistoryBox() {
        historyBox.removeAllItems();
        historyBox.addItem("Seç...");
        for (String mood : history.getMoods()) {
            historyBox.addItem(mood);
        }
    }

    private void showFilms(List<Film> films) {
        resultsPanel.removeAll();
        for (Film film : films) {
            resultsPanel.add(new FilmCard(film));
        }
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }
}
