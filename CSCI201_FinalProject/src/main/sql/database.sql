-- Create Database called "FinalProject"
DROP DATABASE IF EXISTS FinalProject;
CREATE DATABASE FinalProject;
USE FinalProject;

-- Create User Table
CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    lastfm_session_key VARCHAR(128)
);

-- Create Table for Recommendation History
CREATE TABLE search_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL, -- title for songs = "Song Title - Artist", title for movies = "Movie Title"
    recommendations TEXT, -- in json format
    rec_type VARCHAR(255), -- song-to-movie or movie-to-song
    FOREIGN KEY (user_id) REFERENCES user(email)
);
