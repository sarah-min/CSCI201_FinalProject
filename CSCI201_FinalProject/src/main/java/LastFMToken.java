// get token from last.fm, lasts 60 minutes

public class LastFMToken {
	String key = "6aaf096bf740fe27fb746c05f24fcecb";
	String authurl = "http://www.last.fm/api/auth/?api_key=" + key + "&cb=";
	String callbackurl = "/";
	
	public LastFMToken() {
		
	}
	
}