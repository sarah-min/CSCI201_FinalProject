-- Create Database called "FinalProject"
DROP DATABASE IF EXISTS FinalProject;
CREATE DATABASE FinalProject;
USE FinalProject;

-- Create All Tables
CREATE TABLE UserInfo (
    userID int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    fullname varchar(48) NOT NULL,
    email varchar(48) NOT NULL,
    password varchar(16) NOT NULL,
    lastfmSessionKey VARCHAR(128)
);

CREATE TABLE PastQueries (
    userID int NOT NULL
    queryNumber int NOT NULL AUTO_INCREMENT PRIMARY KEY,
    querySummary VARCHAR(128)
    queryResult VARCHAR(256)
    FOREIGN KEY (userID) REFERENCES UserInfo(userID)
);



