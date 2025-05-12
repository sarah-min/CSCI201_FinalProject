import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;
import com.google.gson.*;
import java.io.Writer;
import java.util.Scanner;


class DBUtil {
    // Connection URL for FinalProject database
    static String URL = "jdbc:mysql://127.0.0.1:3306/FinalProject?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
    static String USERNAME = "root";  
    static String PASSWORD = "root";  

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Use this for all operations since database already exists
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}


@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    public RegisterServlet() {
        super();
    }
    
    @Override
    public void init() throws ServletException {
        SystemBootstrapper.initializeDatabaseSchema();
    }
    
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        HeaderConfigurator.configureCrossOrigin(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
    
    @Override
    protected void doPost(HttpServletRequest incomingRequest, HttpServletResponse outgoingResponse) 
            throws ServletException, IOException {
        HeaderConfigurator.configureCrossOrigin(outgoingResponse);
        outgoingResponse.setContentType("application/json");
        outgoingResponse.setCharacterEncoding("UTF-8");
        
        JsonElement clientData = readRequestPayload(incomingRequest);
        JsonObject accountInfo = clientData.getAsJsonObject();
        
        String userAccount = accountInfo.get("username").getAsString();
        String emailAddress = accountInfo.get("email").getAsString();
        String secretKey = accountInfo.get("password").getAsString();
        
        Writer responseWriter = outgoingResponse.getWriter();
        
        if (AccountValidator.checkEmailExists(emailAddress)) {
            responseWriter.write(MessageGenerator.createErrorMessage("Email already registered."));
            return;
        }
        
        boolean accountCreated = AccountHelper.saveNewUser(
            userAccount, emailAddress, secretKey, incomingRequest
        );
        
        responseWriter.write(accountCreated ? 
            MessageGenerator.createSuccessMessage("Registration successful.") :
            MessageGenerator.createErrorMessage("Registration failed.")
        );
    }
    
    private JsonElement readRequestPayload(HttpServletRequest req) throws IOException {
        Scanner scanner = new Scanner(req.getInputStream(), "UTF-8");
        scanner.useDelimiter("\\A");
        String payload = scanner.hasNext() ? scanner.next() : "";
        scanner.close();
        return JsonParser.parseString(payload);
    }
}

class HeaderConfigurator {
    static void configureCrossOrigin(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }
}

class MessageGenerator {
    static String createSuccessMessage(String msg) {
        return createMessage(true, msg);
    }
    
    static String createErrorMessage(String msg) {
        return createMessage(false, msg);
    }
    
    private static String createMessage(boolean isSuccess, String text) {
        JsonObject json = new JsonObject();
        json.addProperty("success", isSuccess);
        json.addProperty("message", text);
        return json.toString();
    }
}

class AccountValidator {
    static boolean checkEmailExists(String email) {
        try (Connection dbConn = DBUtil.getConnection();
             PreparedStatement query = dbConn.prepareStatement(
                 "SELECT COUNT(*) FROM FinalProject.user WHERE email = ?")) {
            
            query.setString(1, email);
            ResultSet result = query.executeQuery();
            
            if (result.next()) {
                return result.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}

class AccountHelper {
    static boolean saveNewUser(String name, String email, String pass, HttpServletRequest req) {
        String insertQuery = "INSERT INTO FinalProject.user (username, email, password) VALUES (?, ?, ?)";
        
        try (Connection dbConn = DBUtil.getConnection();
             PreparedStatement stmt = dbConn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, pass);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int newUserId = generatedKeys.getInt(1);
                    HttpSession userSession = req.getSession(true);
                    userSession.setAttribute("userId", newUserId);
                    return true;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}

class SystemBootstrapper {
	static void initializeDatabaseSchema() {
	    try (Connection conn = DBUtil.getConnection();
	         Statement executor = conn.createStatement()) {
	        
	        executor.executeUpdate("CREATE DATABASE IF NOT EXISTS FinalProject");
	        executor.execute("USE FinalProject");
	        
	        executor.executeUpdate("""
	            CREATE TABLE IF NOT EXISTS user (
	                id INT AUTO_INCREMENT PRIMARY KEY,
	                username VARCHAR(255) NOT NULL,
	                email VARCHAR(255) NOT NULL UNIQUE,
	                password VARCHAR(255) NOT NULL
	            )
	        """);
	        
	        executor.executeUpdate("""
	            CREATE TABLE search_history (
				    id INT AUTO_INCREMENT PRIMARY KEY,
				    email VARCHAR(255) NOT NULL,
				    title VARCHAR(255) NOT NULL, -- title for songs = "Song Title - Artist", title for movies = "Movie Title"
				    recommendations TEXT, -- in json format
				    rec_type VARCHAR(255), -- song-to-movie or movie-to-song
				    FOREIGN KEY (user_id) REFERENCES user(id)
				)
	        """);
	    } catch (SQLException ex) {
	        ex.printStackTrace();
	    }
	}
}