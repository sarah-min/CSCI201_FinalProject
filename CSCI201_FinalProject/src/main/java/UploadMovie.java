

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/UploadMediaTitle")
public class UploadMovie extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	protected void service(HttpServletRequest request, HttpServletResponse response)
		      throws ServletException, IOException {
		String key = "6aaf096bf740fe27fb746c05f24fcecb";
		
		String title = ""; // get from front end 
		
		
		
	}

}
