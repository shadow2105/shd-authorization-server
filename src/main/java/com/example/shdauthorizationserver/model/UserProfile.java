package com.example.shdauthorizationserver.model;

import org.springframework.util.Assert;

public class UserProfile extends BaseEntityAudit {

    private final String username;     // PK, FK
    private final String givenName;
    private final String familyName;

    private String middleName;
    private String email;
    private boolean isProfileComplete;

    public UserProfile(String username, String givenName, String familyName, String middleName, String email,
                       boolean isProfileComplete) {
        Assert.isTrue(username != null && !username.isEmpty() &&
                        givenName != null && !givenName.isEmpty() &&
                        familyName != null && !familyName.isEmpty() &&
                        email != null,
                "Cannot pass null or empty values to constructor");
        this.username = username;
        this.givenName = givenName;
        this.familyName = familyName;
        this.middleName = (middleName == null ? "" : middleName);
        this.email = email;
        this.isProfileComplete = isProfileComplete;
    }

    public String getUsername() {
        return username;
    }

    public String getGivenName() {
        return givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isProfileComplete() {
        return isProfileComplete;
    }

    public void setProfileComplete(boolean profileComplete) {
        isProfileComplete = profileComplete;
    }

    @Override
    public String toString() {
        return "UserProfile{" +
                "username='" + username + '\'' +
                ", givenName='" + givenName + '\'' +
                ", familyName='" + familyName + '\'' +
                ", middleName='" + middleName + '\'' +
                ", email='" + email + '\'' +
                ", isProfileComplete=" + isProfileComplete +
                '}';
    }
}
