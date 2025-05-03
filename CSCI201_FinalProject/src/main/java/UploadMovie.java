// Also mostly working now. Like UploadSong.java, it now only returns json object

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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

@WebServlet("/UploadMovie")
public class UploadMovie extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Gemini API key
    private static final String GEMINI_API_KEY = "AIzaSyC-ftDJUaIIxy3B05R1GLfN5GVzhZfxEsE";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    
    // TMDB API for movie information
    private static final String TMDB_API_KEY = "8cfafa4110bee58be65872baeec97ee1"; 
    private static final String TMDB_API_URL = "https://api.themoviedb.org/3";
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        // Get the movie input from the request
        String movieInput = request.getParameter("movieInput");
        
        if (movieInput == null || movieInput.trim().isEmpty()) {
            sendErrorResponse(out, "No movie input provided");
            return;
        }
        
        try {
            // Get movie details and genres from TMDB
            JsonObject movieDetails = getMovieDetails(movieInput);
            
            if (movieDetails == null) {
                sendErrorResponse(out, "Could not find information for this movie");
                return;
            }
            
            String movieTitle = movieDetails.get("title").getAsString();
            List<String> genres = extractGenres(movieDetails);
            
            // Get song recommendations based on movie genres
            String songRecommendations = getSongRecommendations(genres, movieTitle);
            
            // Save the search to history if user is logged in
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute("userId") != null) {
                int userId = (int) session.getAttribute("userId");
                saveSearchToHistory(userId, movieTitle, genres, songRecommendations);
            }
            
            // Send success response with song recommendations
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("movie", movieTitle);
            
            JsonArray genresArray = new JsonArray();
            for (String genre : genres) {
                genresArray.add(genre);
            }
            jsonResponse.add("genres", genresArray);
            
            jsonResponse.addProperty("recommendations", songRecommendations);
            
            out.write(jsonResponse.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(out, "Error processing request: " + e.getMessage());
        }
    }
    
    private JsonObject getMovieDetails(String movieTitle) throws IOException {
        // URL encode the movie title
        String encodedTitle = URLEncoder.encode(movieTitle, StandardCharsets.UTF_8).replace("+", "%20");
        
        // First search for the movie
        String searchUrl = TMDB_API_URL + "/search/movie?api_key=" + TMDB_API_KEY + "&query=" + encodedTitle;
        
        URL url = new URL(searchUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        
        // Check if the request was successful
        if (conn.getResponseCode() != 200) {
            conn.disconnect();
            throw new IOException("Failed to search for movie. HTTP error code: " + conn.getResponseCode());
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
        JsonObject searchResults = gson.fromJson(content.toString(), JsonObject.class);
        
        // Check if we found any results
        JsonArray results = searchResults.getAsJsonArray("results");
        if (results.size() == 0) {
            return null;
        }
        
        // Get the first (most relevant) result
        JsonObject movie = results.get(0).getAsJsonObject();
        int movieId = movie.get("id").getAsInt();
        
        // Now get the detailed movie information
        String detailUrl = TMDB_API_URL + "/movie/" + movieId + "?api_key=" + TMDB_API_KEY;
        
        url = new URL(detailUrl);
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        
        // Check if the request was successful
        if (conn.getResponseCode() != 200) {
            conn.disconnect();
            throw new IOException("Failed to get movie details. HTTP error code: " + conn.getResponseCode());
        }
        
        // Read the response
        isr = new InputStreamReader(conn.getInputStream());
        br = new BufferedReader(isr);
        content = new StringBuilder();
        
        while ((inputLine = br.readLine()) != null) {
            content.append(inputLine);
        }
        
        br.close();
        conn.disconnect();
        
        // Return the movie details
        return gson.fromJson(content.toString(), JsonObject.class);
    }
    
    private List<String> extractGenres(JsonObject movieDetails) {
        List<String> genres = new ArrayList<>();
        
        if (movieDetails.has("genres")) {
            JsonArray genresArray = movieDetails.getAsJsonArray("genres");
            
            for (JsonElement genreElement : genresArray) {
                JsonObject genreObject = genreElement.getAsJsonObject();
                String genreName = genreObject.get("name").getAsString();
                genres.add(genreName);
            }
        }
        
        return genres;
    }
    
    private String getSongRecommendations(List<String> genres, String movieTitle) throws IOException {
        // Prepare the prompt for Gemini API
        String prompt = "Give a list of 3 song names with their artists based on this movie: '" + 
                        movieTitle + "' with these genres: " + String.join(", ", genres) + 
                        ". Format the response as JSON with an array of songs, where each song has title and artist properties. Only recommend real songs that have been released.";
        
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
    
    private void saveSearchToHistory(int userId, String movieTitle, List<String> genres, String recommendations) {
        try (Connection conn = DBUtil.getConnection()) {
            String query = "INSERT INTO movie_search_history (user_id, movie, genres, recommendations, search_date) " +
                           "VALUES (?, ?, ?, ?, NOW())";
            
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            stmt.setString(2, movieTitle);
            stmt.setString(3, String.join(", ", genres));
            stmt.setString(4, recommendations);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            // Log the error but don't fail the request
            e.printStackTrace();
        }
    }
    
    private void sendErrorResponse(PrintWriter out, String errorMessage) {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", false);
        jsonResponse.addProperty("error", errorMessage);
        out.write(jsonResponse.toString());
    }
}