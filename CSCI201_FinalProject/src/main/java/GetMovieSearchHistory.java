import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/GetMovieSearchHistory")
public class GetMovieSearchHistory extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendErrorResponse(out, "Not logged in");
            return;
        }
        
        int userId = (int) session.getAttribute("userId");
        
        try {
            // Get user's search history from database
            JsonArray history = getMovieSearchHistory(userId);
            
            // Send success response with history
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", true);
            jsonResponse.add("history", history);
            
            out.write(jsonResponse.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(out, "Error processing request: " + e.getMessage());
        }
    }
    
    private JsonArray getMovieSearchHistory(int userId) throws SQLException {
        JsonArray historyArray = new JsonArray();
        
        try (Connection conn = DBUtil.getConnection()) {
            // Query to get user's search history, ordered by most recent first
            String query = "SELECT movie, genres, recommendations, search_date FROM movie_search_history " +
                           "WHERE user_id = ? ORDER BY search_date DESC";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                JsonObject searchEntry = new JsonObject();
                searchEntry.addProperty("movie", rs.getString("movie"));
                searchEntry.addProperty("genres", rs.getString("genres"));
                searchEntry.addProperty("recommendations", rs.getString("recommendations"));
                searchEntry.addProperty("date", rs.getTimestamp("search_date").toString());
                
                historyArray.add(searchEntry);
            }
        }
        
        return historyArray;
    }
    
    private void sendErrorResponse(PrintWriter out, String errorMessage) {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", false);
        jsonResponse.addProperty("error", errorMessage);
        out.write(jsonResponse.toString());
    }
}