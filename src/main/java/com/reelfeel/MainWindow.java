package com.reelfeel;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.List;

public class MainWindow extends JFrame {

    private final JTextArea moodInput = new JTextArea(3, 40);
    private final JButton recommendButton = new JButton("Film Öner");
    private final JLabel statusLabel = new JLabel(" ");
    private final JPanel resultsPanel = new JPanel();

    public MainWindow() {
        super("ReelFeel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 750);
        setLocationRelativeTo(null);

        JLabel title = new JLabel("Bugün nasıl hissediyorsun?");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));

        moodInput.setLineWrap(true);
        moodInput.setWrapStyleWord(true);

        recommendButton.addActionListener(e -> recommend());

        JPanel buttonRow = new JPanel(new BorderLayout());
        buttonRow.add(statusLabel, BorderLayout.CENTER);
        buttonRow.add(recommendButton, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBorder(BorderFactory.createEmptyBorder(16, 16, 8, 16));
        top.add(title, BorderLayout.NORTH);
        top.add(new JScrollPane(moodInput), BorderLayout.CENTER);
        top.add(buttonRow, BorderLayout.SOUTH);

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

        recommendButton.setEnabled(false);
        statusLabel.setText("Filmler aranıyor...");
        showFilms(List.of());

        // İstekler birkaç saniye sürebilir, arayüz donmasın diye arka planda çalıştırıyoruz
        new SwingWorker<List<Film>, Void>() {
            @Override
            protected List<Film> doInBackground() throws Exception {
                return new GeminiClient().recommend(mood);
            }

            @Override
            protected void done() {
                recommendButton.setEnabled(true);
                statusLabel.setText(" ");
                try {
                    showFilms(get());
                } catch (Exception e) {
                    Throwable cause = e.getCause() != null ? e.getCause() : e;
                    JOptionPane.showMessageDialog(MainWindow.this, cause.getMessage(),
                            "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
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
