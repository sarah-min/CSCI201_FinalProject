package com.cs201.Spotify_matcher;

import java.util.Map;

public class GetPlaylistInfo {
    String id;
    String[] trackIDs;
    Map<String, Map<String, Integer>> trackFeatures;

    // user enters last.fm link
    // http call to get the info from front end input??

    // user enters link, get Spotify ID from link;
    public void parseLink(String link) {
        int s = link.indexOf("playlist") + 9;
        int e = link.indexOf("?", s);
        this.id = link.substring(s+9, e);
    }

    // use Last.fm track.getTags to make call to Last.fm API to get playlist information
    // get tags from song

    // send song info to ai api
}