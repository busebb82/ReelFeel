package com.reelfeel;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // olmazsa Swing'in varsayılan görünümüyle devam eder
            }
            new MainWindow().setVisible(true);
        });
    }
}
