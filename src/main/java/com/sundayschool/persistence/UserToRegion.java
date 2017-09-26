package com.sundayschool.persistence;

public class UserToRegion {
    private int id;
    private String username;
    private String region;

    public UserToRegion() {
    }

    public UserToRegion(String username, String region) {
        this.username = username;
        this.region = region;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
