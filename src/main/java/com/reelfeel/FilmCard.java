package com.reelfeel;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

public class FilmCard extends JPanel {

    public FilmCard(Film film) {
        setLayout(new BorderLayout(12, 0));
        setAlignmentX(LEFT_ALIGNMENT);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(12, 0, 12, 0)));

        if (film.getPoster() != null) {
            JLabel poster = new JLabel(film.getPoster());
            poster.setVerticalAlignment(SwingConstants.TOP);
            add(poster, BorderLayout.WEST);
        }

        // JLabel HTML destekliyor, uzun yazıların alt satıra geçmesi için genişlik veriyoruz
        StringBuilder html = new StringBuilder("<html><body style='width: 400px'>");
        html.append("<b style='font-size: 14px'>").append(escape(film.getTitle())).append("</b>");
        html.append(" (").append(film.getYear()).append(")");
        if (film.getRating() > 0) {
            html.append(" &nbsp;★ ").append(String.format("%.1f", film.getRating()));
        }
        html.append("<p><i>").append(escape(film.getReason())).append("</i></p>");
        if (film.getOverview() != null && !film.getOverview().isBlank()) {
            html.append("<p>").append(escape(film.getOverview())).append("</p>");
        }
        html.append("</body></html>");

        JLabel text = new JLabel(html.toString());
        text.setVerticalAlignment(SwingConstants.TOP);
        add(text, BorderLayout.CENTER);
    }

    // BoxLayout kartları dikeyde uzatmasın
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    private static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
