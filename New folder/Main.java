package com.percentagecalculator;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        try {
            DB.initialize();

            SwingUtilities.invokeLater(() -> {
                new PercentageCalculatorApp();
            });

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Database connection failed: " + e.getMessage()
            );
        }
    }
}