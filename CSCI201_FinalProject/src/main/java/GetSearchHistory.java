import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
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
    	
    	String userId = request.getParameter("user");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
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
    
    private JsonArray getSearchHistory(String userId) throws SQLException {
        JsonArray historyArray = new JsonArray();
        
      
    	try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/finalproject?user=root&password=root");
	
	        String query = "SELECT title, recommendations, rec_type " +
	                "FROM search_history " +
	                "WHERE user_id = ?";
	        
	        PreparedStatement stmt = conn.prepareStatement(query);
	        stmt.setString(1, userId);
	        
	        ResultSet rs = stmt.executeQuery();
	        
	        while (rs.next()) {
	            JsonObject searchEntry = new JsonObject();
	           	            
	            searchEntry.addProperty("title", rs.getString("title"));
	            searchEntry.addProperty("recommendations", rs.getString("recommendations"));
	            searchEntry.addProperty("rec_type", rs.getString("rec_type"));
	            
	            historyArray.add(searchEntry);
	        }
        
    	} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return historyArray;
    }
    
    String getJsonString(com.google.gson.JsonObject json, String key) {
        return json.has(key) ? json.get(key).getAsString() : "";
    }
    
    private void sendErrorResponse(PrintWriter out, String errorMessage) {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", false);
        jsonResponse.addProperty("error", errorMessage);
        out.write(jsonResponse.toString());
    }
}