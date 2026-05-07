package cinema;

import javax.swing.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.Cursor;
import java.awt.GraphicsEnvironment;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import cinema.servlet.MovieServlet;

public class Main {
    private static Server jettyServer;

    public static void main(String[] args) throws Exception {
        String portEnv = System.getenv("PORT");
        int port = (portEnv != null) ? Integer.parseInt(portEnv) : 9090;

        if (GraphicsEnvironment.isHeadless()) {
            startServer(port);
            jettyServer.join();
        } else {
            SwingUtilities.invokeLater(() -> createAndShowGUI(port));
        }
    }

    private static void createAndShowGUI(int port) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { e.printStackTrace(); }
        
        JFrame frame = new JFrame("CINÉMA Control Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 150);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        frame.getContentPane().setBackground(new Color(8, 8, 14));

        JLabel statusLabel = new JLabel("Status: Server Stopped");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(new Color(160, 144, 112));
        
        JButton btnStart = new JButton("▶ Start Server");
        btnStart.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnStart.setBackground(new Color(40, 167, 69));
        btnStart.setForeground(Color.WHITE);
        btnStart.setFocusPainted(false);
        btnStart.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnStart.addActionListener(e -> {
            if (jettyServer == null || !jettyServer.isStarted()) {
                btnStart.setEnabled(false);
                statusLabel.setText("Status: Starting...");
                new Thread(() -> {
                    try {
                        startServer(port);
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Status: Running on Port " + port);
                            statusLabel.setForeground(new Color(40, 167, 69));
                            btnStart.setText("■ Stop Server");
                            btnStart.setBackground(new Color(220, 53, 69));
                            btnStart.setEnabled(true);
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() -> {
                            statusLabel.setText("Status: Error starting server");
                            statusLabel.setForeground(Color.RED);
                            btnStart.setEnabled(true);
                        });
                    }
                }).start();
            } else {
                try {
                    jettyServer.stop();
                    statusLabel.setText("Status: Server Stopped");
                    statusLabel.setForeground(new Color(160, 144, 112));
                    btnStart.setText("▶ Start Server");
                    btnStart.setBackground(new Color(40, 167, 69));
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        frame.add(statusLabel);
        frame.add(btnStart);
        frame.setVisible(true);
    }

    private static void startServer(int port) throws Exception {
        jettyServer = new Server(port);
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");
        handler.addServlet(new ServletHolder(new MovieServlet()), "/*");
        jettyServer.setHandler(handler);
        jettyServer.start();
        System.out.println("🎬 CINÉMA Server started on port " + port);
    }
}
