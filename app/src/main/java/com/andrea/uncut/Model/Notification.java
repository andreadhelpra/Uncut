package com.andrea.uncut.Model;

public class Notification {
    private String userid; // ID of the user to get notified
    private String text; // Text of the notification
    private String postid; // ID of the post
    private boolean ispost; // Specify if notification concerns a post

    // Constructor
    public Notification(String userid, String text, String postid, boolean ispost) {
        this.userid = userid;
        this.text = text;
        this.postid = postid;
        this.ispost = ispost;
    }

    // Empty constructor
    public Notification() {
    }

    // Getters and setters. Variables are the same as how they have been stored in the db
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public boolean isIspost() {
        return ispost;
    }

    public void setIspost(boolean ispost) {
        this.ispost = ispost;
    }
}
