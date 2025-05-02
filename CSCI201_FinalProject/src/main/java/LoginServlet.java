import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;
import com.google.gson.*;
import java.nio.charset.StandardCharsets;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private interface SecurityConfig {
        String ALLOWED_ORIGIN = "http://localhost:3000";
        String ALLOWED_METHODS = "POST, GET, OPTIONS, PUT, DELETE";
        String ALLOWED_HEADERS = "Content-Type, Authorization, X-Requested-With";
    }
    
    private class RequestProcessor {
        private final HttpServletRequest req;
        private final HttpServletResponse res;
        
        RequestProcessor(HttpServletRequest req, HttpServletResponse res) {
            this.req = req;
            this.res = res;
        }
        
        void setupHeaders() {
            res.addHeader("Access-Control-Allow-Origin", SecurityConfig.ALLOWED_ORIGIN);
            res.addHeader("Access-Control-Allow-Methods", SecurityConfig.ALLOWED_METHODS);
            res.addHeader("Access-Control-Allow-Headers", SecurityConfig.ALLOWED_HEADERS);
            res.addHeader("Access-Control-Allow-Credentials", "true");
        }
        
        JsonObject parseBody() throws IOException {
            StringBuilder data = new StringBuilder();
            String line;
            var reader = req.getReader();
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }
            return JsonParser.parseString(data.toString()).getAsJsonObject();
        }
    }
    
    private class DatabaseAuthenticator {
        boolean validateCredentials(String user, String pass) {
            Connection connection = null;
            PreparedStatement query = null;
            ResultSet result = null;
            
            try {
                connection = DBUtil.getConnection();
                query = connection.prepareStatement(
                    "SELECT id FROM FinalProject.user WHERE email = ? AND password = ?"
                );
                query.setString(1, user);
                query.setString(2, pass);
                result = query.executeQuery();
                
                if (result.next()) {
                    return result.getInt("id") > 0;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                closeResources(connection, query, result);
            }
            return false;
        }
        
        int fetchUserId(String user, String pass) {
            try (Connection conn = DBUtil.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id FROM FinalProject.user WHERE email = ? AND password = ?"
                );
                stmt.setString(1, user);
                stmt.setString(2, pass);
                
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("id");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return -1;
        }
        
        private void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse res) 
            throws ServletException, IOException {
        new RequestProcessor(req, res).setupHeaders();
        res.setStatus(HttpServletResponse.SC_OK);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) 
            throws ServletException, IOException {
        RequestProcessor processor = new RequestProcessor(req, res);
        processor.setupHeaders();
        
        res.setContentType("application/json");
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        try {
            JsonObject userData = processor.parseBody();
            String emailAddress = userData.get("email").getAsString();
            String passwordInput = userData.get("password").getAsString();
            
            DatabaseAuthenticator auth = new DatabaseAuthenticator();
            int userId = auth.fetchUserId(emailAddress, passwordInput);
            
            if (userId > 0) {
                createSession(req, userId);
                sendResponse(res, true, "Login successful.");
            } else {
                sendResponse(res, false, "Invalid email or password.");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Added detailed error logging
            sendResponse(res, false, "Error processing request: " + e.getMessage());
        }
    }
    
    private void createSession(HttpServletRequest req, int userId) {
        HttpSession session = req.getSession();
        session.setAttribute("userId", userId);
    }
    
    private void sendResponse(HttpServletResponse res, boolean status, String text) 
            throws IOException {
        JsonObject output = new JsonObject();
        output.addProperty("success", status);
        output.addProperty("message", text);
        res.getWriter().print(output.toString());
    }
}