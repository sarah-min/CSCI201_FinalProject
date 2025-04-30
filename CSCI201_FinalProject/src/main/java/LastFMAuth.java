import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


// get token from last.fm, lasts 60 minutes

public class LastFMAuth {
	String key = "6aaf096bf740fe27fb746c05f24fcecb";
	String authurl = "http://ws.audioscrobbler.com/2.0/?method=auth.gettoken&api_key=" + key + "&format=json";
	String sharedSecret = "e0cce0e78b1b83cc33c6fb90279c5a04";
	
	public LastFMAuth() {
		
	}
	
	// tokens valid for 60 minutes + user specific
	public void getToken() {
		try {
			URI uri = new URI(authurl);
			HttpRequest request = HttpRequest.newBuilder().uri(uri).header("api_key", key).GET().build();
			HttpClient client = HttpClient.newHttpClient();
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

			String responseBody = response.body();
	        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
	        String token = jsonResponse.get("token").getAsString();
			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// sessions valid for indefinite length + use token to start session

	public void getSession() {
		
	}
	
}