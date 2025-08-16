// File: ResourceManagementApp.java
package company;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class Project extends JFrame {

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Timeline", "Quantity", "Cost"}, 0);
    private final JTable resourceTable = new JTable(tableModel);
    private final String username;
    private final boolean isAdmin;

    private final String DB_URL = "jdbc:mysql://localhost:3306/company_db?serverTimezone=UTC";
    private final String DB_USER = "process.env.user";
    private final String DB_PASS = "process.env.password";

    public Project(String username, boolean isAdmin) {
        this.username = username;
        this.isAdmin = isAdmin;

        setTitle(isAdmin ? "🔧 Admin Dashboard" : "👤 User Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with buttons
        JPanel topPanel = new JPanel(new FlowLayout());
        JButton viewBtn = new JButton("📋 View Resources");
        viewBtn.addActionListener(this::listResources);
        topPanel.add(viewBtn);

        if (isAdmin) {
            JButton insertBtn = new JButton("➕ Insert Resource");
            JButton deleteBtn = new JButton("🗑️ Delete Resource");
            JButton viewReqBtn = new JButton("📨 View Requests");

            insertBtn.addActionListener(this::insertResource);
            deleteBtn.addActionListener(this::deleteResourceById);
            viewReqBtn.addActionListener(this::viewRequests);

            topPanel.add(insertBtn);
            topPanel.add(deleteBtn);
            topPanel.add(viewReqBtn);
        } else {
            JButton requestBtn = new JButton("🔐 Request Access");
            requestBtn.addActionListener(this::requestAccess);
            topPanel.add(requestBtn);
        }

        JScrollPane scrollPane = new JScrollPane(resourceTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Resources"));

        JButton logoutBtn = new JButton("🔙 Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            showLogin();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(logoutBtn);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private void insertResource(ActionEvent e) {
        JTextField name = new JTextField();
        JTextField timeline = new JTextField();
        JTextField quantity = new JTextField();
        JTextField cost = new JTextField();

        Object[] fields = {
                "Name:", name,
                "Timeline:", timeline,
                "Quantity:", quantity,
                "Cost:", cost
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Insert Resource", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sql = "INSERT INTO resource (resource_name, timeline, quantity, cost) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, name.getText());
                stmt.setString(2, timeline.getText());
                stmt.setInt(3, Integer.parseInt(quantity.getText()));
                stmt.setDouble(4, Double.parseDouble(cost.getText()));
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "✅ Resource added.");
                listResources(null);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
            }
        }
    }

    private void deleteResourceById(ActionEvent e) {
        String input = JOptionPane.showInputDialog(this, "Enter Resource ID to delete:");
        if (input != null && !input.trim().isEmpty()) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
                String sql = "DELETE FROM resource WHERE resource_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, Integer.parseInt(input));
                int rows = stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, rows > 0 ? "🗑️ Deleted." : "⚠️ ID not found.");
                listResources(null);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
            }
        }
    }

    private void listResources(ActionEvent e) {
        tableModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM resource");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("resource_id"),
                        rs.getString("resource_name"),
                        rs.getString("timeline"),
                        rs.getInt("quantity"),
                        rs.getDouble("cost")
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
        }
    }

    private void requestAccess(ActionEvent e) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "INSERT INTO access_requests (username) VALUES (?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Request sent to admin.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "✅ Request recorded.");
        }
    }

    private void viewRequests(ActionEvent e) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM access_requests ORDER BY request_time DESC");

            StringBuilder sb = new StringBuilder();
            while (rs.next()) {
                sb.append("User: ").append(rs.getString("username"))
                        .append(" | Time: ").append(rs.getTimestamp("request_time")).append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.length() == 0 ? "No requests." : sb.toString());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
        }
    }

    private static void showLogin() {
        JFrame frame = new JFrame("🔐 Login");
        frame.setSize(300, 180);
        frame.setLayout(new GridLayout(4, 2, 10, 10));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();
        JButton loginBtn = new JButton("Login");

        loginBtn.addActionListener(e -> {
            String user = userField.getText();
            String pass = new String(passField.getPassword());

            if ("root".equals(user) && "1234".equals(pass)) {
                frame.dispose();
                new Project(user, true);
            } else if (
                    ("user1".equals(user) && "1234".equals(pass)) ||
                            ("user2".equals(user) && "12345".equals(pass))) {
                frame.dispose();
                new Project(user, false);
            } else {
                JOptionPane.showMessageDialog(frame, "❌ Invalid credentials.");
            }
        });

        frame.add(userLabel);
        frame.add(userField);
        frame.add(passLabel);
        frame.add(passField);
        frame.add(new JLabel());
        frame.add(loginBtn);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "❌ MySQL driver not found.");
            return;
        }
        SwingUtilities.invokeLater(Project::showLogin);
    }
}
