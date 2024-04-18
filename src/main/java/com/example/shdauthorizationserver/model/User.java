package com.example.shdauthorizationserver.model;

public class User {

    private final UserAccount userAccount;

    private final UserProfile userProfile;

    public User(UserAccount userAccount, UserProfile userProfile) {
        this.userAccount = userAccount;
        this.userProfile = userProfile;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }
}
