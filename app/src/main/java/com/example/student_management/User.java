package com.example.student_management;

public class User {
    // Attrs
    private String uid;
    private String fullname;
    private String email;
    private String role;
    private String status;
    private String picture;

    // Constructor
    public User(String uid, String fullname, String email, String role, String status, String picture) {
        this.uid = uid;
        this.fullname = fullname;
        this.email = email;
        this.role = role;
        this.status = status;
        this.picture = picture;
    }

    // Getters and Setters
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
