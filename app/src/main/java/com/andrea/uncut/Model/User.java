package com.andrea.uncut.Model;

public class User {
    private String id; // The ID of the user
    private String username; // The username
    private String fullname; // The full name
    private String imageurl; // The profile image url stored in firebase storage
    private String bio; // The bio of the user

    // Constructor
    public User(String id, String username, String fullname, String imageurl, String bio) {
        this.id = id;
        this.username=username;
        this.fullname = fullname;
        this.imageurl = imageurl;
        this.bio=bio;
    }

    // Empty constructor
    private User() {
    }

    // Getters and setters. Variables are the same as how they have been stored in the db
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
