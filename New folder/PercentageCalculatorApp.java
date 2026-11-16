
package com.percentagecalculator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;

public class PercentageCalculatorApp extends JFrame {

    private static final Color BG = new Color(15, 23, 42);
    private static final Color SIDEBAR = new Color(17, 24, 39);
    private static final Color PANEL = new Color(30, 41, 59);
    private static final Color PURPLE = new Color(124, 92, 255);
    private static final Color WHITE = new Color(241, 245, 249);
    private static final Color MUTED = new Color(148, 163, 184);

    private final DecimalFormat df = new DecimalFormat("#,##0.##");

    private JPanel content;
    private JPanel sidebar;
    private int currentUserId;
    private String currentUsername;

    public PercentageCalculatorApp() {
        showLogin();
    }

    // ---------------- LOGIN ----------------

    private void showLogin() {
        setTitle("Percentify | Login");
        setSize(850, 560);
        setMinimumSize(new Dimension(750, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new GridLayout(1, 2));
        main.setBackground(BG);

        JPanel branding = new JPanel();
        branding.setBackground(new Color(37, 31, 78));
        branding.setLayout(new BoxLayout(branding, BoxLayout.Y_AXIS));
        branding.setBorder(new EmptyBorder(65, 45, 45, 35));

        JLabel logo = new JLabel("◈ PERCENTIFY");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 28));

        JLabel tagline = new JLabel(
                "<html><div style='width:260px'>"
                        + "<h1>Make every<br>percentage count.</h1>"
                        + "<p>Smart calculations. Clear insights.<br>"
                        + "Your numbers, made simple.</p></div></html>"
        );
        tagline.setForeground(new Color(220, 220, 255));
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 19));

        branding.add(logo);
        branding.add(Box.createVerticalStrut(65));
        branding.add(tagline);

        JPanel login = new JPanel();
        login.setBackground(BG);
        login.setLayout(new BoxLayout(login, BoxLayout.Y_AXIS));
        login.setBorder(new EmptyBorder(80, 45, 40, 45));

        JLabel heading = new JLabel("Welcome back");
        heading.setFont(new Font("Segoe UI", Font.BOLD, 28));
        heading.setForeground(WHITE);

        JLabel sub = new JLabel("Sign in to your calculator workspace");
        sub.setForeground(MUTED);

        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();

        styleField(username);
        styleField(password);

        JButton loginButton = button("Sign In", PURPLE);

        login.add(heading);
        login.add(Box.createVerticalStrut(10));
        login.add(sub);
        login.add(Box.createVerticalStrut(35));
        login.add(label("USERNAME"));
        login.add(Box.createVerticalStrut(8));
        login.add(username);
        login.add(Box.createVerticalStrut(20));
        login.add(label("PASSWORD"));
        login.add(Box.createVerticalStrut(8));
        login.add(password);
        login.add(Box.createVerticalStrut(28));
        login.add(loginButton);

        loginButton.addActionListener(e -> {
            String user = username.getText().trim();
            String pass = new String(password.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                message("Enter your username and password.");
                return;
            }

            authenticate(user, pass);
        });

        main.add(branding);
        main.add(login);

        setContentPane(main);
        setVisible(true);
    }

    private void authenticate(String username, String password) {
        try {
            String sql =
                    "SELECT id FROM users WHERE username=? AND password_hash=?";

            try (Connection con = DB.connect();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, username);
                ps.setString(2, DB.hash(password));

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentUserId = rs.getInt("id");
                        currentUsername = username;
                        showDashboard();
                    } else {
                        message("Invalid username or password.");
                    }
                }
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    // ---------------- MAIN DASHBOARD ----------------

    private void showDashboard() {
        getContentPane().removeAll();

        setTitle("Percentify | Dashboard");
        setSize(1200, 760);
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(225, 0));
        sidebar.setBackground(SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(25, 15, 20, 15));

        JLabel brand = new JLabel("◈ PERCENTIFY");
        brand.setForeground(Color.WHITE);
        brand.setFont(new Font("Segoe UI", Font.BOLD, 21));
        brand.setBorder(new EmptyBorder(5, 8, 35, 5));

        sidebar.add(brand);
        sidebar.add(navButton("⌂   Dashboard", this::showHome));
        sidebar.add(navButton("▦   Calculators", this::showCalculators));
        sidebar.add(navButton("◷   History", this::showHistory));

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(navButton("↪   Logout", this::logout));

        content = new JPanel(new BorderLayout());
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(28, 32, 28, 32));

        root.add(sidebar, BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);

        setContentPane(root);
        showHome();

        revalidate();
        repaint();
    }

    private void showHome() {
        content.removeAll();

        JPanel page = new JPanel();
        page.setBackground(BG);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));

        JLabel title = heading("Welcome, " + currentUsername + " 👋");
        JLabel subtitle = normalText(
                "Your personal percentage calculation workspace."
        );

        page.add(title);
        page.add(Box.createVerticalStrut(7));
        page.add(subtitle);
        page.add(Box.createVerticalStrut(25));

        JPanel stats = new JPanel(new GridLayout(1, 3, 15, 0));
        stats.setBackground(BG);

        stats.add(statCard("TOTAL CALCULATIONS",
                String.valueOf(getCalculationCount()), "All-time activity"));

        stats.add(statCard("YOUR WORKSPACE",
                "7 Tools", "Ready to calculate"));

        stats.add(statCard("ACCOUNT",
                currentUsername, "Logged-in user"));

        page.add(stats);
        page.add(Box.createVerticalStrut(30));

        JLabel toolsTitle = heading("Explore your calculators");
        page.add(toolsTitle);
        page.add(Box.createVerticalStrut(15));

        JPanel cards = new JPanel(new GridLayout(0, 2, 15, 15));
        cards.setBackground(BG);

        String[] names = {
                "Basic Percentage",
                "Percentage Increase",
                "Percentage Decrease",
                "Combined Calculator",
                "Discount Calculator",
                "CGPA Converter",
                "Marks Percentage"
        };

        String[] descriptions = {
                "Find a percentage of any number.",
                "Calculate percentage growth.",
                "Find the percentage reduction.",
                "Compare two different values.",
                "Calculate discount and final price.",
                "Convert CGPA into percentage.",
                "Calculate marks and scores."
        };

        for (int i = 0; i < names.length; i++) {
            final int index = i;
            cards.add(calculatorCard(
                    names[i], descriptions[i],
                    () -> openCalculator(index)
            ));
        }

        page.add(cards);
        page.add(Box.createVerticalStrut(20));

        JButton historyButton = button(
                "View Calculation History  →", new Color(59, 130, 246)
        );
        historyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        historyButton.addActionListener(e -> showHistory());

        page.add(historyButton);

        JScrollPane scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);

        content.add(scroll, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    // ---------------- CALCULATORS ----------------

    private void showCalculators() {
        content.removeAll();

        JPanel page = new JPanel(new BorderLayout(0, 20));
        page.setBackground(BG);

        JPanel top = new JPanel();
        top.setBackground(BG);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        top.add(heading("All Calculators"));
        top.add(Box.createVerticalStrut(8));
        top.add(normalText("Choose a tool to start calculating."));

        JPanel cards = new JPanel(new GridLayout(0, 2, 15, 15));
        cards.setBackground(BG);

        String[] names = {
                "Basic Percentage",
                "Percentage Increase",
                "Percentage Decrease",
                "Combined Calculator",
                "Discount Calculator",
                "CGPA Converter",
                "Marks Percentage"
        };

        String[] descriptions = {
                "Find a percentage of a number.",
                "Measure percentage growth.",
                "Measure percentage reduction.",
                "Compare original and new values.",
                "Find discount and final amount.",
                "Convert CGPA to percentage.",
                "Find your marks percentage."
        };

        for (int i = 0; i < names.length; i++) {
            final int index = i;
            cards.add(calculatorCard(
                    names[i], descriptions[i],
                    () -> openCalculator(index)
            ));
        }

        page.add(top, BorderLayout.NORTH);
        page.add(cards, BorderLayout.CENTER);

        content.add(page, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();
    }

    private void openCalculator(int type) {
        String[] names = {
                "Basic Percentage",
                "Percentage Increase",
                "Percentage Decrease",
                "Combined Calculator",
                "Discount Calculator",
                "CGPA Converter",
                "Marks Percentage"
        };

        String[] fields = switch (type) {
            case 0 -> new String[]{"Percentage", "Number"};
            case 1, 2, 3 -> new String[]{"Original Value", "New Value"};
            case 4 -> new String[]{"Original Price", "Discount Percentage"};
            case 5 -> new String[]{"CGPA"};
            case 6 -> new String[]{"Marks Obtained", "Total Marks"};
            default -> new String[]{};
        };

        JPanel form = new JPanel(new GridLayout(fields.length, 2, 10, 12));
        form.setBackground(PANEL);
        form.setBorder(new EmptyBorder(15, 15, 15, 15));

        JTextField[] inputs = new JTextField[fields.length];

        for (int i = 0; i < fields.length; i++) {
            JLabel lbl = new JLabel(fields[i]);
            lbl.setForeground(WHITE);

            inputs[i] = new JTextField();
            styleField(inputs[i]);

            form.add(lbl);
            form.add(inputs[i]);
        }

        JLabel result = new JLabel("Enter values and calculate.");
        result.setForeground(new Color(134, 239, 172));
        result.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JButton calculate = button("Calculate & Save", PURPLE);

        JPanel wrapper = new JPanel(new BorderLayout(0, 15));
        wrapper.setBackground(PANEL);
        wrapper.setBorder(new EmptyBorder(20, 20, 20, 20));
        wrapper.add(form, BorderLayout.NORTH);
        wrapper.add(result, BorderLayout.CENTER);
        wrapper.add(calculate, BorderLayout.SOUTH);

        calculate.addActionListener(e -> {
            try {
                double[] v = new double[inputs.length];

                for (int i = 0; i < inputs.length; i++) {
                    v[i] = Double.parseDouble(inputs[i].getText().trim());
                }

                double answer;
                String detail;

                switch (type) {
                    case 0 -> {
                        answer = v[0] / 100.0 * v[1];
                        detail = "Percentage = " + v[0]
                                + "%, Number = " + v[1];
                    }
                    case 1 -> {
                        if (v[0] == 0) throw new ArithmeticException(
                                "Original value cannot be zero."
                        );
                        answer = (v[1] - v[0]) / v[0] * 100;
                        detail = "Original = " + v[0]
                                + ", New = " + v[1];
                    }
                    case 2 -> {
                        if (v[0] == 0) throw new ArithmeticException(
                                "Original value cannot be zero."
                        );
                        answer = (v[0] - v[1]) / v[0] * 100;
                        detail = "Original = " + v[0]
                                + ", New = " + v[1];
                    }
                    case 3 -> {
                        if (v[0] == 0) throw new ArithmeticException(
                                "Original value cannot be zero."
                        );
                        answer = (v[1] - v[0]) / v[0] * 100;
                        detail = "Original = " + v[0]
                                + ", New = " + v[1];
                    }
                    case 4 -> {
                        double discount = v[0] * v[1] / 100;
                        answer = v[0] - discount;
                        detail = "Price = " + v[0]
                                + ", Discount = " + v[1]
                                + "%, Discount amount = " + df.format(discount);
                    }
                    case 5 -> {
                        answer = v[0] * 9.5;
                        detail = "CGPA = " + v[0];
                    }
                    case 6 -> {
                        if (v[1] == 0) throw new ArithmeticException(
                                "Total marks cannot be zero."
                        );
                        if (v[0] > v[1]) throw new ArithmeticException(
                                "Obtained marks cannot exceed total marks."
                        );
                        answer = v[0] / v[1] * 100;
                        detail = "Obtained = " + v[0]
                                + ", Total = " + v[1];
                    }
                    default -> throw new IllegalArgumentException(
                            "Invalid calculator."
                    );
                }

                if (!Double.isFinite(answer)) {
                    throw new ArithmeticException("Invalid result.");
                }

                String suffix = (type == 0 || type == 5 ||
                        type == 6 || type == 1 || type == 2 || type == 3)
                        ? "%" : "";

                String output;

                if (type == 4) {
                    output = "Final price: " + df.format(answer)
                            + " (discount applied)";
                } else {
                    output = "Result: " + df.format(answer) + suffix;
                }

                result.setText(output);

                saveCalculation(names[type], detail, output);

            } catch (NumberFormatException ex) {
                message("Please enter valid numbers in every field.");
            } catch (Exception ex) {
                message(ex.getMessage() == null
                        ? "Calculation failed." : ex.getMessage());
            }
        });

        JOptionPane.showMessageDialog(
                this, wrapper, names[type], JOptionPane.PLAIN_MESSAGE
        );
    }

    // ---------------- HISTORY ----------------

    private void showHistory() {
        content.removeAll();

        JPanel page = new JPanel(new BorderLayout(0, 18));
        page.setBackground(BG);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG);

        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(BG);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(heading("Calculation History"));
        titlePanel.add(Box.createVerticalStrut(7));
        titlePanel.add(normalText("Your saved calculations from MySQL."));

        JTextField search = new JTextField(18);
        styleField(search);

        JButton refresh = button("Refresh", PURPLE);
        JButton clear = button("Clear History", new Color(190, 55, 75));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setBackground(BG);
        actions.add(search);
        actions.add(refresh);
        actions.add(clear);

        top.add(titlePanel, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);

        String[] columns = {
                "Calculator", "Inputs", "Result", "Date"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setBackground(PANEL);
        table.setForeground(WHITE);
        table.setGridColor(new Color(51, 65, 85));
        table.setRowHeight(34);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setBackground(new Color(51, 65, 85));
        table.getTableHeader().setForeground(WHITE);
        table.setSelectionBackground(PURPLE);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(PANEL);
        scroll.setBorder(BorderFactory.createLineBorder(
                new Color(51, 65, 85)
        ));

        Runnable load = () -> {
            model.setRowCount(0);

            try {
                String sql =
                        "SELECT module_name, input_values, result, calculated_at "
                                + "FROM calculation_history "
                                + "WHERE user_id=? "
                                + "ORDER BY calculated_at DESC";

                try (Connection con = DB.connect();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    ps.setInt(1, currentUserId);

                    try (ResultSet rs = ps.executeQuery()) {
                        String query = search.getText()
                                .trim().toLowerCase();

                        while (rs.next()) {
                            String module = rs.getString("module_name");
                            String input = rs.getString("input_values");
                            String result = rs.getString("result");
                            String date = rs.getString("calculated_at");

                            String combined = (module + " " + input
                                    + " " + result).toLowerCase();

                            if (query.isEmpty()
                                    || combined.contains(query)) {
                                model.addRow(new Object[]{
                                        module, input, result, date
                                });
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                showError(ex);
            }
        };

        refresh.addActionListener(e -> load.run());
        search.addActionListener(e -> load.run());

        clear.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete all your history?",
                    "Confirm deletion",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                clearHistory();
                load.run();
            }
        });

        page.add(top, BorderLayout.NORTH);
        page.add(scroll, BorderLayout.CENTER);

        content.add(page, BorderLayout.CENTER);
        content.revalidate();
        content.repaint();

        load.run();
    }

    private void saveCalculation(
            String module, String inputs, String result
    ) {
        String sql =
                "INSERT INTO calculation_history "
                        + "(user_id, module_name, input_values, result) "
                        + "VALUES (?, ?, ?, ?)";

        try (Connection con = DB.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, currentUserId);
            ps.setString(2, module);
            ps.setString(3, inputs);
            ps.setString(4, result);

            ps.executeUpdate();

        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void clearHistory() {
        String sql =
                "DELETE FROM calculation_history WHERE user_id=?";

        try (Connection con = DB.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, currentUserId);
            ps.executeUpdate();

            message("Calculation history cleared.");

        } catch (Exception ex) {
            showError(ex);
        }
    }

    private int getCalculationCount() {
        String sql =
                "SELECT COUNT(*) FROM calculation_history WHERE user_id=?";

        try (Connection con = DB.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, currentUserId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (Exception ex) {
            showError(ex);
        }

        return 0;
    }

    // ---------------- UI HELPERS ----------------

    private JPanel statCard(String name, String value, String note) {
        JPanel card = new JPanel();
        card.setBackground(PANEL);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel heading = label(name);
        JLabel number = new JLabel(value);
        number.setForeground(Color.WHITE);
        number.setFont(new Font("Segoe UI", Font.BOLD, 25));

        JLabel detail = normalText(note);

        card.add(heading);
        card.add(Box.createVerticalStrut(12));
        card.add(number);
        card.add(Box.createVerticalStrut(6));
        card.add(detail);

        return card;
    }

    private JPanel calculatorCard(
            String name, String description, Runnable action
    ) {
        JPanel card = new JPanel(new BorderLayout(10, 12));
        card.setBackground(PANEL);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(51, 65, 85)
                ),
                new EmptyBorder(17, 17, 17, 17)
        ));

        JLabel title = new JLabel(name);
        title.setForeground(WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));

        JLabel desc = normalText(description);

        JButton open = button("Open Calculator  →", PURPLE);
        open.addActionListener(e -> action.run());

        JPanel center = new JPanel();
        center.setBackground(PANEL);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(title);
        center.add(Box.createVerticalStrut(8));
        center.add(desc);

        card.add(center, BorderLayout.CENTER);
        card.add(open, BorderLayout.SOUTH);

        return card;
    }

    private JButton navButton(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(200, 45));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBackground(SIDEBAR);
        b.setForeground(WHITE);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 12, 10, 5));
        b.addActionListener(e -> action.run());
        return b;
    }

    private JButton button(String text, Color color) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(11, 17, 11, 17));
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 25));
        return l;
    }

    private JLabel normalText(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MUTED);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MUTED);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        return l;
    }

    private void styleField(JTextField field) {
        field.setBackground(new Color(51, 65, 85));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(new EmptyBorder(11, 12, 11, 12));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        field.setPreferredSize(new Dimension(200, 42));
    }

    private void message(String text) {
        JOptionPane.showMessageDialog(
                this, text, "Percentify",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(
                this,
                "An error occurred:\n" + ex.getMessage(),
                "Application Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Do you want to log out?",
                "Logout",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            currentUserId = 0;
            currentUsername = null;
            showLogin();
        }
    }
}
