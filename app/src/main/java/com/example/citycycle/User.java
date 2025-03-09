package com.example.citycycle;

public class User {
    private String username;
    private int mobile;
    private String address;
    private String email;
    private String password;

    public User() {}

    public User(String username, int mobile, String address, String email, String password) {
        this.username = username;
        this.mobile = mobile;
        this.address = address;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getMobile() {
        return mobile;
    }

    public void setMobile(int mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
