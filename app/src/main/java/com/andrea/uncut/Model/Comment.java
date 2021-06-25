package com.andrea.uncut.Model;

public class Comment {

    private String comment; // Text of the comment
    private String publisher; // Publisher of the comment
    private String commentid; // ID of the comment

    // Constructor
    public Comment(String comment, String publisher, String commentid) {
        this.comment = comment;
        this.publisher = publisher;
        this.commentid = commentid;
    }

    // Empty constructor
    public Comment() {
    }

    // Getters and setters. Variables are the same as how they have been stored in the db
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getCommentid() {
        return commentid;
    }

    public void setCommentid(String commentid) {
        this.commentid = commentid;
    }
}
