import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/GetSearchHistory")
public class GetSearchHistory extends HttpServlet {
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
//        
        int userId = (int) session.getAttribute("userId");
        
        try {
            // Get user's song search history from database
            JsonArray history = getSearchHistory(userId);
            
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
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
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
        String searchQuery = request.getParameter("query");
        String resultsJson = request.getParameter("results");
        
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            sendErrorResponse(out, "No search query provided");
            return;
        }
        
        try {
            // Save the search query and its results
            int searchId = saveSongSearch(userId, searchQuery, resultsJson);
            
            if (searchId > 0) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Search history saved successfully");
                jsonResponse.addProperty("searchId", searchId);
                
                out.write(jsonResponse.toString());
            } else {
                sendErrorResponse(out, "Failed to save search history");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(out, "Error processing request: " + e.getMessage());
        }
    }
    
    private JsonArray getSearchHistory(int userId) throws SQLException {
        JsonArray historyArray = new JsonArray();
        
        try (Connection conn = DBUtil.getConnection()) {
            // Query to get user's song search history, ordered by most recent first
            String query = "SELECT ssh.id, ssh.search_query, ssh.result_count, ssh.search_date " +
                           "FROM song_search_history ssh " +
                           "WHERE ssh.user_id = ? " +
                           "ORDER BY ssh.search_date DESC";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                JsonObject searchEntry = new JsonObject();
                int searchId = rs.getInt("id");
                
                searchEntry.addProperty("id", searchId);
                searchEntry.addProperty("query", rs.getString("search_query"));
                searchEntry.addProperty("resultCount", rs.getInt("result_count"));
                searchEntry.addProperty("date", rs.getTimestamp("search_date").toString());
                
                // Get the search results for this search
                JsonArray resultsArray = getSongSearchResults(searchId);
                searchEntry.add("results", resultsArray);
                
                historyArray.add(searchEntry);
            }
        }
        
        return historyArray;
    }
    
    private JsonArray getSongSearchResults(int searchId) throws SQLException {
        JsonArray resultsArray = new JsonArray();
        
        try (Connection conn = DBUtil.getConnection()) {
            String query = "SELECT artist_name, track_name, album_name, track_url, artist_url, image_url " +
                           "FROM song_search_results " +
                           "WHERE search_id = ?";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, searchId);
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                JsonObject result = new JsonObject();
                result.addProperty("artist", rs.getString("artist_name"));
                result.addProperty("track", rs.getString("track_name"));
                result.addProperty("album", rs.getString("album_name"));
                result.addProperty("trackUrl", rs.getString("track_url"));
                result.addProperty("artistUrl", rs.getString("artist_url"));
                result.addProperty("imageUrl", rs.getString("image_url"));
                
                resultsArray.add(result);
            }
        }
        
        return resultsArray;
    }
    
    private int saveSongSearch(int userId, String searchQuery, String resultsJson) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int searchId = -1;
        
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            
            // Insert the search query
            String insertQuery = "INSERT INTO song_search_history (user_id, search_query, result_count) " +
                                 "VALUES (?, ?, ?)";
            
            stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, userId);
            stmt.setString(2, searchQuery);
            
            // Parse the results JSON to count the number of results
            com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
            com.google.gson.JsonElement jsonElement = parser.parse(resultsJson);
            com.google.gson.JsonArray resultsArray = jsonElement.getAsJsonArray();
            int resultCount = resultsArray.size();
            
            stmt.setInt(3, resultCount);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    searchId = rs.getInt(1);
                    
                    // Now insert each search result
                    for (int i = 0; i < resultsArray.size(); i++) {
                        com.google.gson.JsonObject result = resultsArray.get(i).getAsJsonObject();
                        
                        String insertResultQuery = "INSERT INTO song_search_results " +
                                                  "(search_id, artist_name, track_name, album_name, track_url, artist_url, image_url) " +
                                                  "VALUES (?, ?, ?, ?, ?, ?, ?)";
                        
                        PreparedStatement resultStmt = conn.prepareStatement(insertResultQuery);
                        resultStmt.setInt(1, searchId);
                        resultStmt.setString(2, getJsonString(result, "artist"));
                        resultStmt.setString(3, getJsonString(result, "track"));
                        resultStmt.setString(4, getJsonString(result, "album"));
                        resultStmt.setString(5, getJsonString(result, "trackUrl"));
                        resultStmt.setString(6, getJsonString(result, "artistUrl"));
                        resultStmt.setString(7, getJsonString(result, "imageUrl"));
                        
                        resultStmt.executeUpdate();
                        resultStmt.close();
                        
                        // Optionally save tags for each song if they exist
                        if (result.has("tags")) {
                            com.google.gson.JsonArray tags = result.getAsJsonArray("tags");
                            for (int j = 0; j < tags.size(); j++) {
                                String tagName = tags.get(j).getAsString();
                                saveSongTag(conn, getJsonString(result, "track"), getJsonString(result, "artist"), tagName);
                            }
                        }
                    }
                }
            }
            
            conn.commit();
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return searchId;
    }
    
    private void saveSongTag(Connection conn, String trackName, String artistName, String tagName) throws SQLException {
        String query = "INSERT INTO song_tags (track_name, artist_name, tag_name) VALUES (?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE tag_count = tag_count + 1";
        
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, trackName);
        stmt.setString(2, artistName);
        stmt.setString(3, tagName);
        
        stmt.executeUpdate();
        stmt.close();
    }
    
    private String getJsonString(com.google.gson.JsonObject json, String key) {
        return json.has(key) ? json.get(key).getAsString() : "";
    }
    
    private void sendErrorResponse(PrintWriter out, String errorMessage) {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", false);
        jsonResponse.addProperty("error", errorMessage);
        out.write(jsonResponse.toString());
    }
}