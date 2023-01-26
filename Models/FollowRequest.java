package com.mustafa.message_app.Models;

public class FollowRequest {
    public String requestEmail,requestName,senderEmail,senderName,requestNumber;


    public FollowRequest(String requestEmail, String requestName,String senderEmail,String senderName, String requestNumber) {
        this.requestEmail = requestEmail;
        this.requestName = requestName;
        this.requestNumber = requestNumber;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
    }
}
