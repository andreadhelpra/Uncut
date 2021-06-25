package com.andrea.uncut.Model;

public class Post {
    private String  postID; // ID of the post
    private String  postImage; // Image of the post
    private float  score; // Rating of the post
    private String  title; // Title of the post
    private String  description; // Review of the post
    private String  publisher; // Publisher of the post

    // Constructor
    public Post(String postID, String postImage, float score, String title, String description, String publisher) {
        this.postID = postID;
        this.postImage = postImage;
        this.score = score;
        this.title = title;
        this.description = description;
        this.publisher = publisher;
    }

    // Empty constructor
    public Post() {
    }

    // Getters and setters. Variables are the same as how they have been stored in the db
    public String getPostid() {
        return postID;
    }

    public void setPostid(String postID) {
        this.postID = postID;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public float getScore() { return score; }

    public void setScore(float score) {
        this.score = score;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
