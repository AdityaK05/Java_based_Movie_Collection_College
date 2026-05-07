package cinema.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import cinema.model.MovieData;

public class MovieManager {
    private Connection conn;

    public MovieManager() {
        try {
            Class.forName("org.postgresql.Driver");
            String dbUrl = System.getenv("DATABASE_URL");
            
            if (dbUrl != null && (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://"))) {
                // Parse Render or Railway URL
                // postgres://user:password@host:port/database
                String cleanUrl = dbUrl.replaceFirst("postgresql?://", "");
                String[] parts = cleanUrl.split("@");
                String[] auth = parts[0].split(":");
                String url = "jdbc:postgresql://" + parts[1];
                if (!url.contains("?")) {
                    url += "?sslmode=disable";
                } else {
                    url += "&sslmode=disable";
                }
                
                conn = DriverManager.getConnection(url, auth[0], auth[1]);
            } else if (System.getenv("JDBC_DATABASE_URL") != null) {
                // Spring Boot style env var
                conn = DriverManager.getConnection(System.getenv("JDBC_DATABASE_URL"));
            } else {
                // Fallback for local development
                conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/movie", "postgres", "postgres");
            }

            Statement stmt = conn.createStatement();
            // PostgreSQL uses SERIAL instead of AUTOINCREMENT
            stmt.execute("CREATE TABLE IF NOT EXISTS movies (id SERIAL PRIMARY KEY, title TEXT NOT NULL, genre TEXT NOT NULL, rating REAL, year INTEGER)");
            
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM movies");
            if (rs.next() && rs.getInt(1) == 0) {
                loadSampleData();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not connect to PostgreSQL database. Please verify your DATABASE_URL environment variable.");
        }
    }

    private void loadSampleData() {
        addMovie("Inception", "Sci-Fi", 8.8, 2010);
        addMovie("Interstellar", "Sci-Fi", 8.6, 2014);
        addMovie("The Dark Knight", "Action", 9.0, 2008);
        addMovie("Avengers: Endgame", "Action", 8.4, 2019);
        addMovie("Joker", "Drama", 8.5, 2019);
        addMovie("Titanic", "Romance", 7.8, 1997);
        addMovie("The Godfather", "Crime", 9.2, 1972);
        addMovie("Parasite", "Thriller", 8.6, 2019);
    }

    public void addMovie(String title, String genre, double rating, int year) {
        if (conn == null) return;
        try {
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO movies (title, genre, rating, year) VALUES (?, ?, ?, ?)");
            pstmt.setString(1, title);
            pstmt.setString(2, genre);
            pstmt.setDouble(3, rating);
            pstmt.setInt(4, year);
            pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void removeById(int id) {
        if (conn == null) return;
        try {
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM movies WHERE id = ?");
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public int count() {
        if (conn == null) return 0;
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM movies");
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }
    
    public double getAvgRating() {
        if (conn == null) return 0;
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT AVG(rating) FROM movies");
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }
    
    public String getTopGenre() {
        if (conn == null) return "—";
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT genre, COUNT(*) as c FROM movies GROUP BY genre ORDER BY c DESC LIMIT 1");
            if (rs.next()) return rs.getString(1);
        } catch (Exception e) { e.printStackTrace(); }
        return "—";
    }

    public List<MovieData> getMovies(String mode) {
        List<MovieData> list = new ArrayList<>();
        if (conn == null) return list;
        try {
            String query = "SELECT * FROM movies";
            if ("Sort by rating ↓".equals(mode)) query += " ORDER BY rating DESC";
            else if ("Sort by year ↑".equals(mode)) query += " ORDER BY year ASC";
            else if ("Rating > 8.0 ⭐".equals(mode)) query += " WHERE rating > 8.0";
            
            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                list.add(new MovieData(rs.getInt("id"), rs.getString("title"), rs.getString("genre"), rs.getDouble("rating"), rs.getInt("year")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}
