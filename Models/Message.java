package com.mustafa.message_app.Models;

public class Message {
    public String text,fromUID,date,otherEmail;

    public Message(String text, String fromUID, String date, String otherEmail) {
        this.text = text;
        this.fromUID = fromUID;
        this.date = date;
        this.otherEmail = otherEmail;
    }
}
