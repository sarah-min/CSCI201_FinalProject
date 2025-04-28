import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

@WebServlet ("/UploadSong")
public class UploadSong extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	// api call format:
	// http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=6aaf096bf740fe27fb746c05f24fcecb&artist=p1harmony&track=love+me+for+me&format=json

	
	protected void service(HttpServletRequest request, HttpServletResponse response)
		      throws ServletException, IOException {
		String key = "6aaf096bf740fe27fb746c05f24fcecb";
 
		String artist = "";
		String track = ""; 
		String urlStr = "http://ws.audioscrobbler.com/2.0/?method=track.getInfo&api_key=" + key + "&artist=" + artist + "&track=" + track + "&format=json";
		URL url = new URL(urlStr);
		Gson gson = new Gson();
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");
		
		InputStreamReader isr = new InputStreamReader(conn.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = br.readLine()) != null) {
            content.append(inputLine);
        }
        br.close();
        conn.disconnect();
        
        JsonObject jsonObject = gson.fromJson(content.toString(), JsonObject.class);
        JsonArray tagArray = jsonObject.getAsJsonObject("track").getAsJsonObject("toptags").getAsJsonArray("tag");
        
        // list of tags for this song
        List<String> tags = new ArrayList<>();
        for (JsonElement tag : tagArray) {
            JsonObject tagObject = tag.getAsJsonObject();
            String newTag = tagObject.get("name").getAsString();
            tags.add(newTag);
        }
        
	}
	
	class ArtsyResponse {
	    Embedded _embedded;

	    static class Embedded {
	        List<Result> results;
	    }

	    static class Result {
	        String title;
	        String type;
	        Links _links;

	        static class Links {
	            Href thumbnail;
	            Href permalink;

	            static class Href {
	                String href;
	            }
	        }
	    }
	}
	
}