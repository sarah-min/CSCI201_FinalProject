-- Create Database called "FinalProject"
DROP DATABASE IF EXISTS FinalProject;
CREATE DATABASE FinalProject;
USE FinalProject;

-- Create User Table
CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    lastfm_session_key VARCHAR(128)
);

-- Create Favorites Table
CREATE TABLE favorites (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    artist_id VARCHAR(100) NOT NULL,
    artist_name VARCHAR(255) NOT NULL,
    artist_image VARCHAR(500),
    FOREIGN KEY (user_id) REFERENCES user(id),
    UNIQUE KEY unique_favorite (user_id, artist_id)
);

-- Create API Token Storage Table
CREATE TABLE artsy_token (
    id INT PRIMARY KEY DEFAULT 1,
    token TEXT NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

-- Create Table for Music-to-Movie Recommendations History
CREATE TABLE search_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    artist VARCHAR(255) NOT NULL,
    track VARCHAR(255) NOT NULL,
    tags TEXT,
    recommendations TEXT,
    search_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- Create Table for Movie-to-Music Recommendations History
CREATE TABLE movie_search_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    movie VARCHAR(255) NOT NULL,
    genres TEXT,
    recommendations TEXT,
    search_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- Create Table for Song Search History
CREATE TABLE song_search_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    search_query VARCHAR(255) NOT NULL,
    result_count INT,
    search_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- Create Table for Song Search Results (detailed results from each search)
CREATE TABLE song_search_results (
    id INT AUTO_INCREMENT PRIMARY KEY,
    search_id INT NOT NULL,
    artist_name VARCHAR(255) NOT NULL,
    track_name VARCHAR(255) NOT NULL,
    album_name VARCHAR(255),
    track_url VARCHAR(500),
    artist_url VARCHAR(500),
    image_url VARCHAR(500),
    FOREIGN KEY (search_id) REFERENCES song_search_history(id)
);

-- Create Table for Playlist Recommendations
CREATE TABLE playlist_recommendations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    playlist_name VARCHAR(255),
    playlist_url VARCHAR(500),
    recommendations TEXT,
    search_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- Create Table for User Settings
CREATE TABLE user_settings (
    user_id INT PRIMARY KEY,
    theme VARCHAR(20) DEFAULT 'light',
    recommendation_count INT DEFAULT 3,
    last_login TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- Create Table for Song Tags (for more detailed analysis)
CREATE TABLE song_tags (
    id INT AUTO_INCREMENT PRIMARY KEY,
    track_name VARCHAR(255) NOT NULL,
    artist_name VARCHAR(255) NOT NULL,
    tag_name VARCHAR(100) NOT NULL,
    tag_count INT DEFAULT 1,
    UNIQUE KEY unique_song_tag (track_name, artist_name, tag_name)
);