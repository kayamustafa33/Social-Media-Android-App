package com.mustafa.message_app.Models;

public class UserSavedImages {

    public String savedDownloadUrl,savedEmail,sharedEmail,sharedName;

    public UserSavedImages(String savedDownloadUrl, String savedEmail, String sharedEmail, String sharedName) {
        this.savedDownloadUrl = savedDownloadUrl;
        this.savedEmail = savedEmail;
        this.sharedEmail = sharedEmail;
        this.sharedName = sharedName;
    }
}
