import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.Instant;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebServlet("/spotify-callback")
public class SpotifyAuthServlet extends HttpServlet {
    private static final String CLIENT_ID = "6aaf096bf740fe27fb746c05f24fcecb";
    private static final String CLIENT_SECRET = "e0cce0e78b1b83cc33c6fb90279c5a04";
    private static final String REDIRECT_URI = "http://localhost:8080/your-app/spotify-callback";
    private static final String SPOTIFY_TOKEN_URL = "https://accounts.spotify.com/api/token";
    
    @Override
    public void init() throws ServletException {
        initializeSpotifyTokenTable();
    }
    
    private void initializeSpotifyTokenTable() {
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS spotify_tokens (" +
                "user_id INT PRIMARY KEY, " +
                "access_token VARCHAR(255) NOT NULL, " +
                "refresh_token VARCHAR(255) NOT NULL, " +
                "expires_at TIMESTAMP NOT NULL, " +
                "FOREIGN KEY (user_id) REFERENCES user(id))"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("code");
        String error = req.getParameter("error");
        
        if (error != null) {
            resp.sendRedirect("error.html");
            return;
        }
        
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect("login.html");
            return;
        }
        int userId = (Integer) session.getAttribute("userId");
        
        try {
            JsonObject tokenResponse = exchangeCodeForTokens(code);
            String accessToken = tokenResponse.get("access_token").getAsString();
            String refreshToken = tokenResponse.get("refresh_token").getAsString();
            long expiresIn = tokenResponse.get("expires_in").getAsLong();
            Timestamp expiresAt = Timestamp.from(Instant.now().plusSeconds(expiresIn));
            
            saveTokens(userId, accessToken, refreshToken, expiresAt);
            resp.sendRedirect("main.html?spotifyConnected=true");
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect("error.html");
        }
    }
    
    private JsonObject exchangeCodeForTokens(String code) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String requestBody = "grant_type=authorization_code" +
                             "&code=" + code +
                             "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8") +
                             "&client_id=" + CLIENT_ID +
                             "&client_secret=" + CLIENT_SECRET;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SPOTIFY_TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new IOException("Failed to exchange code: " + response.body());
        }
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }
    
    private void saveTokens(int userId, String accessToken, String refreshToken, Timestamp expiresAt) {
        String sql = "INSERT INTO spotify_tokens (user_id, access_token, refresh_token, expires_at) " +
                     "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                     "access_token = VALUES(access_token), " +
                     "refresh_token = VALUES(refresh_token), " +
                     "expires_at = VALUES(expires_at)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, accessToken);
            stmt.setString(3, refreshToken);
            stmt.setTimestamp(4, expiresAt);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}