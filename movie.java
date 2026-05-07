import java.io.*;
import java.time.Year;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * CINÉMA - Movie Collection Manager (Standalone Servlet)
 * Single-file application with embedded Jetty server
 */

class MovieData implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String title, genre;
    private final double rating;
    private final int year;

    MovieData(String title, String genre, double rating, int year) {
        if (title == null || title.trim().isEmpty())
            throw new IllegalArgumentException("Title cannot be empty.");
        if (genre == null || genre.trim().isEmpty())
            throw new IllegalArgumentException("Genre cannot be empty.");
        if (rating < 0 || rating > 10)
            throw new IllegalArgumentException("Rating must be between 0 and 10.");
        int maxYear = Year.now().getValue() + 1;
        if (year < 1888 || year > maxYear)
            throw new IllegalArgumentException("Year must be between 1888 and " + maxYear + ".");
        this.title = title.trim();
        this.genre = genre.trim();
        this.rating = rating;
        this.year = year;
    }

    String getTitle() { return title; }
    String getGenre() { return genre; }
    double getRating() { return rating; }
    int getYear() { return year; }
}

class MovieManager {
    private final List<MovieData> movies = new ArrayList<>();

    MovieManager() {
        loadSampleData();
    }

    private void loadSampleData() {
        movies.add(new MovieData("Inception", "Sci-Fi", 8.8, 2010));
        movies.add(new MovieData("Interstellar", "Sci-Fi", 8.6, 2014));
        movies.add(new MovieData("The Dark Knight", "Action", 9.0, 2008));
        movies.add(new MovieData("Avengers: Endgame", "Action", 8.4, 2019));
        movies.add(new MovieData("Joker", "Drama", 8.5, 2019));
        movies.add(new MovieData("Titanic", "Romance", 7.8, 1997));
        movies.add(new MovieData("The Godfather", "Crime", 9.2, 1972));
        movies.add(new MovieData("Parasite", "Thriller", 8.6, 2019));
    }

    void addMovie(MovieData m) { movies.add(m); }
    void removeAt(int i) { if (i >= 0 && i < movies.size()) movies.remove(i); }
    int count() { return movies.size(); }
    
    double getAvgRating() {
        if (movies.isEmpty()) return 0;
        return movies.stream().mapToDouble(MovieData::getRating).average().orElse(0);
    }
    
    String getTopGenre() {
        if (movies.isEmpty()) return "—";
        return movies.stream().collect(
            java.util.stream.Collectors.groupingBy(MovieData::getGenre, java.util.stream.Collectors.counting()))
            .entrySet().stream().max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey).orElse("—");
    }

    List<MovieData> getMovies(String mode) {
        List<MovieData> list = new ArrayList<>(movies);
        if ("Sort by rating ↓".equals(mode))
            list.sort((a, b) -> Double.compare(b.getRating(), a.getRating()));
        else if ("Sort by year ↑".equals(mode))
            list.sort((a, b) -> Integer.compare(a.getYear(), b.getYear()));
        else if ("Rating > 8.0 ⭐".equals(mode))
            list.removeIf(m -> m.getRating() <= 8.0);
        return list;
    }
}

@WebServlet(urlPatterns = {"/", "/movies"})
public class movie extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static MovieManager mgr;

    public static void main(String[] args) throws Exception {
        String portEnv = System.getenv("PORT");
        int port = (portEnv != null) ? Integer.parseInt(portEnv) : 9090;
        Server server = new Server(port);
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        handler.setContextPath("/");
        handler.addServlet(new ServletHolder(new movie()), "/*");
        server.setHandler(handler);
        server.start();
        System.out.println("🎬 CINÉMA Server started on port " + port);
        server.join();
    }

    @Override
    public void init() throws ServletException {
        if (mgr == null) {
            mgr = new MovieManager();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/html;charset=UTF-8");
        
        HttpSession session = req.getSession(true);
        if (session.getAttribute("user") == null) {
            renderLoginPage(req, res);
            return;
        }

        String mode = req.getParameter("viewMode");
        if (mode == null) mode = "All movies";
        
        List<MovieData> list = mgr.getMovies(mode);
        PrintWriter out = res.getWriter();
        
        out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>CINÉMA - Movie Collection</title>");
        out.println("<style>");
        out.println("*{margin:0;padding:0;box-sizing:border-box}");
        out.println("body{font-family:'Segoe UI',sans-serif;background:#08080e;color:#f0e6c8}");
        out.println(".container{display:grid;grid-template-columns:320px 1fr;gap:20px;padding:20px;max-width:1200px;margin:0 auto}");
        out.println("header{grid-column:1/-1;background:linear-gradient(135deg,#1e1605,#0a0a14);padding:30px;border-radius:8px;border-bottom:2px solid #d4a017;position:relative;}");
        out.println("h1{color:#d4a017;font-size:32px;margin-bottom:10px}");
        out.println(".logout-btn{position:absolute;top:30px;right:30px;background:#1c1c2c;color:#d4a017;border:1px solid #d4a017;padding:8px 15px;border-radius:4px;cursor:pointer;font-weight:bold;width:auto;margin-top:0;}");
        out.println(".logout-btn:hover{background:#d4a017;color:#000;}");
        out.println(".tagline{color:#a09070;font-size:13px}");
        out.println(".stats{display:flex;gap:40px;margin-top:20px}");
        out.println(".stat{text-align:center}");
        out.println(".stat-value{color:#d4a017;font-size:20px;font-weight:bold}");
        out.println(".stat-label{color:#a09070;font-size:11px;margin-top:5px}");
        out.println(".sidebar{background:#101a1a;padding:20px;border-radius:8px;border:1px solid #323826;height:fit-content}");
        out.println(".sidebar h3{color:#d4a017;font-size:16px;margin-bottom:15px;border-bottom:1px solid #323826;padding-bottom:10px}");
        out.println(".form-group{margin-bottom:15px}");
        out.println("label{display:block;color:#a09070;font-size:11px;font-weight:bold;margin-bottom:5px}");
        out.println("input,select{width:100%;padding:10px;background:#1c1c2c;color:#f0e6c8;border:1px solid #323826;border-radius:6px;font:inherit}");
        out.println("input:focus,select:focus{outline:0;border-color:#d4a017;box-shadow:0 0 8px rgba(212,160,23,0.4)}");
        out.println("button{width:100%;padding:11px;background:linear-gradient(135deg,#8c6a0f,#d4a017);color:#000;border:0;border-radius:6px;font-weight:bold;cursor:pointer;margin-top:5px}");
        out.println("button:hover{transform:translateY(-2px);box-shadow:0 6px 16px rgba(212,160,23,0.3)}");
        out.println(".main-content{background:#101a1a;padding:20px;border-radius:8px;border:1px solid #323826}");
        out.println(".main-content h2{color:#d4a017;font-size:18px;margin-bottom:15px}");
        out.println("table{width:100%;border-collapse:collapse}");
        out.println("th{background:#08080e;color:#d4a017;padding:12px;text-align:left;font-size:12px;font-weight:bold;border-bottom:1px solid #323826}");
        out.println("td{padding:12px;border-bottom:1px solid #322822}");
        out.println("tr:hover{background-color:rgba(212,160,23,0.08)}");
        out.println(".rating{color:#d4a017;font-weight:bold}");
        out.println(".year{color:#a09070;text-align:center}");
        out.println(".del-btn{padding:5px 10px;background:#dc3c64;color:white;border:0;border-radius:4px;cursor:pointer;font-size:12px;width:auto}");
        out.println(".del-btn:hover{background:#cc2c54}");
        out.println(".empty{text-align:center;padding:40px 20px;color:#a09070}");
        out.println("</style></head><body>");
        
        out.println("<header>");
        out.println("<form method='POST' style='display:inline'><input type='hidden' name='action' value='logout'><button type='submit' class='logout-btn'>Logout</button></form>");
        out.println("<h1>🎬 CINÉMA</h1>");
        out.println("<p class='tagline'>Your Personal Film Collection Manager</p>");
        out.println("<div class='stats'>");
        out.println("<div class='stat'><div class='stat-value'>" + mgr.count() + "</div><div class='stat-label'>FILMS</div></div>");
        out.println("<div class='stat'><div class='stat-value'>" + String.format("%.1f", mgr.getAvgRating()) + "</div><div class='stat-label'>AVG RATING</div></div>");
        out.println("<div class='stat'><div class='stat-value'>" + mgr.getTopGenre() + "</div><div class='stat-label'>TOP GENRE</div></div>");
        out.println("</div></header>");
        
        out.println("<div class='container'>");
        out.println("<aside class='sidebar'>");
        out.println("<h3>Add New Film</h3>");
        out.println("<form method='POST'>");
        out.println("<input type='hidden' name='action' value='add'>");
        out.println("<div class='form-group'><label>Title</label><input type='text' name='title' required></div>");
        out.println("<div class='form-group'><label>Genre</label><select name='genre' required>");
        out.println("<option>Action</option><option>Adventure</option><option>Animation</option><option>Comedy</option>");
        out.println("<option>Crime</option><option>Documentary</option><option>Drama</option><option>Fantasy</option>");
        out.println("<option>Horror</option><option>Mystery</option><option>Romance</option><option>Sci-Fi</option>");
        out.println("<option>Thriller</option><option>Western</option><option>Other</option>");
        out.println("</select></div>");
        out.println("<div class='form-group'><label>Rating (0-10)</label><input type='number' name='rating' min='0' max='10' step='0.1' required></div>");
        out.println("<div class='form-group'><label>Year</label><input type='number' name='year' min='1888' max='" + (Year.now().getValue() + 1) + "' required></div>");
        out.println("<button type='submit'>+ Add Film</button>");
        out.println("<button type='reset' style='background:#323826;color:#a09070'>✕ Clear</button>");
        out.println("</form>");
        
        out.println("<h3 style='margin-top:30px'>View Mode</h3>");
        out.println("<form method='GET'><select name='viewMode' onchange='this.form.submit()'>");
        out.println("<option value='All movies' " + ("All movies".equals(mode) ? "selected" : "") + ">All movies</option>");
        out.println("<option value='Sort by rating ↓' " + ("Sort by rating ↓".equals(mode) ? "selected" : "") + ">Sort by rating ↓</option>");
        out.println("<option value='Sort by year ↑' " + ("Sort by year ↑".equals(mode) ? "selected" : "") + ">Sort by year ↑</option>");
        out.println("<option value='Rating > 8.0 ⭐' " + ("Rating > 8.0 ⭐".equals(mode) ? "selected" : "") + ">Rating > 8.0 ⭐</option>");
        out.println("</select></form></aside>");
        
        out.println("<main class='main-content'>");
        out.println("<h2>Collection</h2>");
        
        String errorMsg = (String) session.getAttribute("error");
        if (errorMsg != null) {
            out.println("<div class='error' style='color:#dc3c64;background:rgba(220,60,100,0.1);padding:10px;border-radius:4px;margin-bottom:20px;'>" + esc(errorMsg) + "</div>");
            session.removeAttribute("error");
        }
        
        if (list.isEmpty()) {
            out.println("<div class='empty'>No movies yet. Add your first film!</div>");
        } else {
            out.println("<table><thead><tr><th>Title</th><th>Genre</th><th>Rating</th><th>Year</th><th>Action</th></tr></thead><tbody>");
            for (int i = 0; i < list.size(); i++) {
                MovieData m = list.get(i);
                out.println("<tr><td>" + esc(m.getTitle()) + "</td><td>" + m.getGenre() + "</td>");
                out.println("<td class='rating'>" + String.format("%.1f", m.getRating()) + " ★</td>");
                out.println("<td class='year'>" + m.getYear() + "</td>");
                out.println("<td><form method='POST' style='display:inline'><input type='hidden' name='action' value='delete'>");
                out.println("<input type='hidden' name='index' value='" + i + "'>");
                out.println("<button type='submit' class='del-btn'>Delete</button></form></td></tr>");
            }
            out.println("</tbody></table>");
        }
        
        out.println("</main></div></body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String action = req.getParameter("action");
        HttpSession session = req.getSession(true);
        
        if ("login".equals(action)) {
            String user = req.getParameter("username");
            String pass = req.getParameter("password");
            if ("admin".equals(user) && "admin123".equals(pass)) {
                session.setAttribute("user", user);
                res.sendRedirect(req.getRequestURI());
            } else {
                res.sendRedirect(req.getRequestURI() + "?error=1");
            }
            return;
        } else if ("logout".equals(action)) {
            session.invalidate();
            res.sendRedirect(req.getRequestURI());
            return;
        }

        if (session.getAttribute("user") != null) {
            if ("add".equals(action)) {
                try {
                    String title = req.getParameter("title");
                    String genre = req.getParameter("genre");
                    double rating = Double.parseDouble(req.getParameter("rating"));
                    int year = Integer.parseInt(req.getParameter("year"));
                    mgr.addMovie(new MovieData(title, genre, rating, year));
                } catch (IllegalArgumentException e) {
                    session.setAttribute("error", e.getMessage());
                } catch (Exception e) {
                    session.setAttribute("error", "Invalid input format.");
                }
            } else if ("delete".equals(action)) {
                try {
                    int idx = Integer.parseInt(req.getParameter("index"));
                    mgr.removeAt(idx);
                } catch (Exception e) {
                    session.setAttribute("error", "Failed to delete film.");
                }
            }
        }
        
        res.sendRedirect(req.getRequestURI());
    }
    
    private void renderLoginPage(HttpServletRequest req, HttpServletResponse res) throws IOException {
        PrintWriter out = res.getWriter();
        out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>CINÉMA - Login</title>");
        out.println("<style>");
        out.println("*{margin:0;padding:0;box-sizing:border-box}");
        out.println("body{font-family:'Segoe UI',sans-serif;background:#08080e;color:#f0e6c8;display:flex;justify-content:center;align-items:center;height:100vh;}");
        out.println(".login-container{background:#101a1a;padding:40px;border-radius:8px;border:1px solid #323826;width:100%;max-width:400px;text-align:center;box-shadow:0 10px 30px rgba(0,0,0,0.5);}");
        out.println("h2{color:#d4a017;margin-bottom:20px;font-size:24px;}");
        out.println(".form-group{margin-bottom:20px;text-align:left;}");
        out.println("label{display:block;color:#a09070;font-size:12px;font-weight:bold;margin-bottom:8px}");
        out.println("input{width:100%;padding:12px;background:#1c1c2c;color:#f0e6c8;border:1px solid #323826;border-radius:6px;font:inherit;transition:all 0.3s;}");
        out.println("input:focus{outline:0;border-color:#d4a017;box-shadow:0 0 8px rgba(212,160,23,0.4)}");
        out.println("button{width:100%;padding:14px;background:linear-gradient(135deg,#8c6a0f,#d4a017);color:#000;border:0;border-radius:6px;font-weight:bold;cursor:pointer;margin-top:10px;font-size:16px;transition:all 0.3s;}");
        out.println("button:hover{transform:translateY(-2px);box-shadow:0 6px 16px rgba(212,160,23,0.3)}");
        out.println(".error{color:#dc3c64;font-size:14px;margin-bottom:15px;background:rgba(220,60,100,0.1);padding:10px;border-radius:4px;}");
        out.println("</style></head><body>");
        out.println("<div class='login-container'>");
        out.println("<h2>🎬 CINÉMA Login</h2>");
        if (req.getParameter("error") != null) {
            out.println("<div class='error'>Invalid username or password.</div>");
        }
        out.println("<form method='POST'>");
        out.println("<input type='hidden' name='action' value='login'>");
        out.println("<div class='form-group'><label>Username</label><input type='text' name='username' required placeholder='admin'></div>");
        out.println("<div class='form-group'><label>Password</label><input type='password' name='password' required placeholder='admin123'></div>");
        out.println("<button type='submit'>Sign In</button>");
        out.println("</form></div></body></html>");
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
