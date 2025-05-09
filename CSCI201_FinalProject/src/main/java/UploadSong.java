//TODO: This is working, but right now only support upload song title, need to also 
// support submitting playlist
//TODO2: Display the results from gemini to a new page. (Right now it is only a JSON object)


import java.io.BufferedReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/UploadSong")
public class UploadSong extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // LastFM API key
    private static final String LASTFM_API_KEY = "6aaf096bf740fe27fb746c05f24fcecb";
    
    // Gemini API key
    private static final String GEMINI_API_KEY = "AIzaSyC-ftDJUaIIxy3B05R1GLfN5GVzhZfxEsE";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        // Get the song input from the request
        String songInput = request.getParameter("songInput");
        
        if (songInput == null || songInput.trim().isEmpty()) {
            sendErrorResponse(out, "No song input provided");
            return;
        }
        
        // Parse song input to get artist and track
        String[] parts = songInput.split("-");
        if (parts.length < 2) {
            sendErrorResponse(out, "Invalid format. Please use 'Song Title - Artist' format");
            return;
        }
        
        String track = parts[0].trim();
        String artist = parts[1].trim();
       
        
        // URL encode the artist and track names
        String parsedartist = URLEncoder.encode(artist, StandardCharsets.UTF_8);
        String parsedtrack = URLEncoder.encode(track, StandardCharsets.UTF_8);
        parsedartist = parsedartist.replaceAll(" ", "+");
        parsedtrack = parsedtrack.replaceAll(" ", "+");
        
        try {
            // Get song tags from LastFM
            List<String> tags = getLastFMTags(parsedartist, parsedtrack);
            
            if (tags.isEmpty()) {
                sendErrorResponse(out, "Could not find tags for this song");
                return;
            }
            
            // Get movie recommendations based on song tags
            String movieRecommendations = getMovieRecommendations(tags);
            
            // Save the search to history if user is logged in
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("userId") != null) {
                int userId = (int) session.getAttribute("userId");
                saveSearchToHistory(userId, artist, track, tags, movieRecommendations);
            }
            
            // Send success response with movie recommendations
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("artist", artist);
            jsonResponse.addProperty("track", track);
            
            JsonArray tagsArray = new JsonArray();
            for (String tag : tags) {
                tagsArray.add(tag);
            }
            jsonResponse.add("tags", tagsArray);
            
            jsonResponse.addProperty("recommendations", movieRecommendations);
            
            out.write(jsonResponse.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(out, "Error processing request: " + e.getMessage());
        }
    }
    
    private List<String> getLastFMTags(String a, String t) throws IOException {
        List<String> tags = new ArrayList<>();
        
        
        // String urlStr = "http://ws.audioscrobbler.com/2.0/";
        String urlStr = "http://ws.audioscrobbler.com/2.0/?method=track.gettoptags&artist=" + 
                        a + "&track=" + t + "&api_key=" + LASTFM_API_KEY + "&format=json";
        
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        
        // Check if the request was successful
        if (conn.getResponseCode() != 200) {
            conn.disconnect();
            throw new IOException("Failed to get tags from LastFM. HTTP error code: " + conn.getResponseCode());
        }
        
        // Read the response
        InputStreamReader isr = new InputStreamReader(conn.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        StringBuilder content = new StringBuilder();
        String inputLine;
        
        while ((inputLine = br.readLine()) != null) {
            content.append(inputLine);
        }
        
        br.close();
        conn.disconnect();
        
        // Parse the JSON response
        Gson gson = new Gson();
        JsonObject jo = gson.fromJson(content.toString(), JsonObject.class);
        
        // Check for errors in the LastFM response
        if (jo.get("error") != null) {
            throw new IOException("LastFM API error: " + jo.get("message").getAsString());
        }
        
        // Extract tags
        if (jo.has("toptags") && jo.getAsJsonObject("toptags").has("tag")) {
            JsonArray tagArray = jo.getAsJsonObject("toptags").getAsJsonArray("tag");
            
            for (JsonElement tag : tagArray) {
                JsonObject tagObject = tag.getAsJsonObject();
                String newTag = tagObject.get("name").getAsString();
                tags.add(newTag);
                
                // Limit to top 10 tags
                if (tags.size() >= 10) {
                    break;
                }
            }
        }
        
        return tags;
    }
    
    private String getMovieRecommendations(List<String> tags) throws IOException {
        // Prepare the prompt for Gemini API
        String prompt = "Give a list of 3 movie names and a short summary of each based on these tags: " + 
                        String.join(", ", tags) + 
                        " without mentioning the tags in your response in json format with the two fields being 'title' and 'summary', only provide real movies that have been released";
        
        // Create the request body
        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);
        parts.add(textPart);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);
        
        // Send request to Gemini API
        URL url = new URL(GEMINI_API_URL + "?key=" + GEMINI_API_KEY);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        
        // Write the request body
        try (PrintWriter pw = new PrintWriter(conn.getOutputStream())) {
            pw.write(requestBody.toString());
        }
        
        // Read the response
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            // If there's an error, try to read the error stream
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line);
                }
                throw new IOException("Gemini API error: " + errorResponse.toString());
            }
        }
        
        // Parse the Gemini response to extract the generated text
        JsonObject responseJson = JsonParser.parseString(response.toString()).getAsJsonObject();
        
        if (responseJson.has("candidates") && responseJson.getAsJsonArray("candidates").size() > 0) {
            JsonObject candidate = responseJson.getAsJsonArray("candidates").get(0).getAsJsonObject();
            if (candidate.has("content") && candidate.getAsJsonObject("content").has("parts")) {
                JsonArray responseParts = candidate.getAsJsonObject("content").getAsJsonArray("parts");
                if (responseParts.size() > 0 && responseParts.get(0).getAsJsonObject().has("text")) {
                    return responseParts.get(0).getAsJsonObject().get("text").getAsString();
                }
            }
        }
        
        // If we couldn't parse the response correctly, return an error
        throw new IOException("Failed to parse Gemini API response");
    }
    
    private void saveSearchToHistory(int userId, String artist, String track, List<String> tags, String recommendations) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            
            // Insert into song_search_history first
            String query = "INSERT INTO song_search_history (user_id, search_query, result_count, search_date) " +
                           "VALUES (?, ?, ?, NOW())";
            
            stmt = conn.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, userId);
            stmt.setString(2, artist + " - " + track);
            stmt.setInt(3, 1); // Just one result for now
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int searchId = rs.getInt(1);
                    
                    // Now insert the search result details
                    String resultQuery = "INSERT INTO song_search_results " +
                                         "(search_id, artist_name, track_name, album_name, track_url, artist_url, image_url) " +
                                         "VALUES (?, ?, ?, ?, ?, ?, ?)";
                    
                    PreparedStatement resultStmt = conn.prepareStatement(resultQuery);
                    resultStmt.setInt(1, searchId);
                    resultStmt.setString(2, artist);
                    resultStmt.setString(3, track);
                    resultStmt.setString(4, ""); // No album info available
                    resultStmt.setString(5, ""); // No track URL available
                    resultStmt.setString(6, ""); // No artist URL available
                    resultStmt.setString(7, ""); // No image URL available
                    
                    resultStmt.executeUpdate();
                    resultStmt.close();
                    
                    // Also save to the old search_history table for backward compatibility
                    String oldQuery = "INSERT INTO search_history (user_id, artist, track, tags, recommendations, search_date) " +
                                     "VALUES (?, ?, ?, ?, ?, NOW())";
                    
                    PreparedStatement oldStmt = conn.prepareStatement(oldQuery);
                    oldStmt.setInt(1, userId);
                    oldStmt.setString(2, artist);
                    oldStmt.setString(3, track);
                    oldStmt.setString(4, String.join(", ", tags));
                    oldStmt.setString(5, recommendations);
                    
                    oldStmt.executeUpdate();
                    oldStmt.close();
                    
                    // Save tags for this search
                    for (String tag : tags) {
                        saveSongTag(conn, track, artist, tag);
                    }
                }
            }
            
            conn.commit();
            
        } catch (SQLException e) {
            // Log the error but don't fail the request
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
    
    private void sendErrorResponse(PrintWriter out, String errorMessage) {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", false);
        jsonResponse.addProperty("error", errorMessage);
        out.write(jsonResponse.toString());
    }
    
    
}