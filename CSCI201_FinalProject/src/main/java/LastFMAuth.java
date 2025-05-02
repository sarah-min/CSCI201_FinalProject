import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class LastFMAuth {
	private String key = "6aaf096bf740fe27fb746c05f24fcecb";
	private String authurl = "http://ws.audioscrobbler.com/2.0/?method=auth.gettoken&api_key=" + key + "&format=json";
	private String sharedSecret = "e0cce0e78b1b83cc33c6fb90279c5a04";
	private String token;
	
	public LastFMAuth() {
		
	}
	
	// tokens valid for 60 minutes + user specific
	public void getToken() {
		try {
			URI uri = new URI(authurl);
			HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			HttpClient client = HttpClient.newHttpClient();
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
	        
	        if (response.statusCode() == 200) {
                String responseBody = response.body();
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                this.token = jsonResponse.get("token").getAsString();
                System.out.println("Token: " + token);
            } else {
                System.err.println("Error: Failed to fetch token. Status code: " + response.statusCode());
            }
			
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
		
		try {
			String apiSig = getAPIsig(token);
			String url = "http://ws.audioscrobbler.com/2.0/?method=auth.getSession" + "&api_key=" + key + "&token=" + token + "&api_sig=" + apiSig + "&format=json";
			URI uri = new URI(url);
			HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			
			HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle the response
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                System.out.println("Response: " + responseBody);
                // Parse the session key from the response (if JSON or XML)
            } else {
                System.err.println("Error: " + response.statusCode() + " - " + response.body());
            }
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
	
	
	// helper function to create api signature using md5 hash
	public String getAPIsig(String token) {
		String sig;
		// "api_key" + apiKey + "methodauth.getSessiontoken" + token + sharedSecret
		String unencrypted = "api_key" + key +  "method" + "auth.getSession" + "token" + token + sharedSecret;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest(unencrypted.getBytes());
	        BigInteger num = new BigInteger(1, digest);
	        sig = num.toString(16);

	        // Add leading zeros to make it a 32-character hash
	        while (sig.length() < 32) {
	            sig = "0" + sig;
	        }
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sig = "error";
		}
		
		return sig;
	}
	
}