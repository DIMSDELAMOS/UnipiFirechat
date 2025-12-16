package com.example.unipifirechat;

public class User {
    public String uid;
    public String username;
    public String email;

    // Default constructor required for Firebase
    public User() {
    }

    public User(String uid, String username, String email) {
        this.uid = uid;
        this.username = username;
        this.email = email;
    }

    // Getters
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
}