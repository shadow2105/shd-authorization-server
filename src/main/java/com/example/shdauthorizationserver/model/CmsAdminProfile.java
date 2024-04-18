package com.example.shdauthorizationserver.model;

import org.springframework.util.Assert;

public class CmsAdminProfile extends UserProfile {

    private String department;

    public CmsAdminProfile(String username, String givenName, String familyName, String middleName, String email,
                           boolean isProfileComplete, String department) {
        super(username, givenName, familyName, middleName, email, isProfileComplete);
        this.department = department;
    }

    public static CmsAdminProfile.UserProfileBuilder builder() {
        return new CmsAdminProfile.UserProfileBuilder();
    }

    @Override
    public String toString() {
        return "CmsAdminProfile{" +
                "username='" + getUsername() + '\'' +
                ", givenName='" + getGivenName() + '\'' +
                ", familyName='" + getFamilyName() + '\'' +
                ", middleName='" + getMiddleName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", department='" + department + '\'' +
                ", isProfileComplete='" + isProfileComplete() + '\'' +
                '}';
    }

    public static final class UserProfileBuilder {

        private String username;
        private String givenName;
        private String familyName;

        private String middleName;
        private String email;

        private String department;

        private boolean isProfileComplete;

        public CmsAdminProfile.UserProfileBuilder username(String username) {
            Assert.notNull(username, "username cannot be null");
            this.username = username;
            return this;
        }

        public CmsAdminProfile.UserProfileBuilder givenName(String givenName) {
            Assert.notNull(givenName, "given name cannot be null");
            this.givenName = givenName;
            return this;
        }

        public CmsAdminProfile.UserProfileBuilder familyName(String familyName) {
            Assert.notNull(familyName, "family name cannot be null");
            this.familyName = familyName;
            return this;
        }

        public CmsAdminProfile.UserProfileBuilder middleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        public CmsAdminProfile.UserProfileBuilder email(String email) {
            Assert.notNull(email, "email cannot be null");
            this.email = email;
            return this;
        }

        public CmsAdminProfile.UserProfileBuilder department(String department) {
            Assert.notNull(department, "department cannot be null");
            this.department = department;
            return this;
        }

        public CmsAdminProfile.UserProfileBuilder isProfileComplete(boolean isProfileComplete) {
            this.isProfileComplete = isProfileComplete;
            return this;
        }

        public CmsAdminProfile build() {
            return new CmsAdminProfile(this.username, this.givenName, this.familyName, this.middleName, this.email,
                    this.isProfileComplete, this.department);
        }
    }
}
