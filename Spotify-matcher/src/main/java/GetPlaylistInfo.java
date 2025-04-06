package com.cs201.Spotify_matcher;

import java.util.Map;

public class GetPlaylistInfo {
    String id;
    String[] trackIDs;
    Map<String, Map<String, Integer>> trackFeatures;

    // user enters link, playlist -> three dots -> share -> copy link to playlist
    // http call to get the info from front end input??

    // user enters link, get Spotify ID from link;
    public void parseLink(String link) {
        int s = link.indexOf("playlist") + 9;
        int e = link.indexOf("?", s);
        this.id = link.substring(s+9, e);
    }

    // use Spotify ID to make call to Spotify API to get playlist information
    // get tracks within playlist

    // parse out audio features of each track in playlist
}