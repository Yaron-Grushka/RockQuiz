package com.quiz.rockquiz;

import android.net.Uri;

public class User {
    private String email;
    private String userName;
    private int highScore;

    public User() {}

    public User(String email, String userName, int highScore) {
        this.email = email;
        this.userName = userName;
        this.highScore = highScore;
    }

    public String getEmail() {
        return email;
    }

    public String getUserName() {
        return userName;
    }

    public int getHighScore() {
        return highScore;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }
}
