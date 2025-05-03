import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

@WebServlet("/connect-spotify")
public class SpotifyConnectServlet extends HttpServlet {
    private static final String CLIENT_ID = "your_spotify_client_id";
    private static final String REDIRECT_URI = "http://localhost:8080/your-app/spotify-callback";
    private static final String SCOPE = "playlist-read-private playlist-modify-public";

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String authUrl = "https://accounts.spotify.com/authorize" +
            "?response_type=code" +
            "&client_id=" + CLIENT_ID +
            "&scope=" + URLEncoder.encode(SCOPE, "UTF-8") +
            "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, "UTF-8");
        resp.sendRedirect(authUrl);
    }
}