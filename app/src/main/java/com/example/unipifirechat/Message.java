// Message.java

package com.example.unipifirechat;

public class Message {
    private String senderId;
    private String recipientId;
    private String text;
    private long timestamp; // Timestamp (για να ταξινομούμε τα μηνύματα)

    // Default constructor (Απαραίτητο για το Firebase)
    public Message() {
    }

    // Constructor με ορίσματα
    public Message(String senderId, String recipientId, String text, long timestamp) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.text = text;
        this.timestamp = timestamp;
    }

    // Getters
    public String getSenderId() {
        return senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getText() {
        return text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters (Χρησιμοποιούνται κυρίως για το Firebase)
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}