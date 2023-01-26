package com.mustafa.message_app.Models;

public class Post {
    public String email,comment,downloadUrl;
    public int likes;

    public Post(String email, String comment, String downloadUrl,int likes) {
        this.email = email;
        this.comment = comment;
        this.downloadUrl = downloadUrl;
        this.likes = likes;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
}
