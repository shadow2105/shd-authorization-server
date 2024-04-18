package com.example.shdauthorizationserver.model;

import org.springframework.util.Assert;

import java.sql.Date;

public class CmsCustomerProfile extends UserProfile {
    private String preferredName;      // set default name as: givenName middleName familyName
    private String picture;            // set default pfp as: specify url here
    private boolean isEmailVerified;
    private Gender gender;
    private Date birthDate;
    private ZoneInfo zoneInfo;          // make user select the zone; retrieving from user-agent can vary the time_zone
    private CustomLocale locale;        // make user select the locale (language preferences) - English or French
    private String phoneNumber;
    private boolean isPhoneVerified;
    private String address;

    private String SIN;           // need to find a way to securely store this - encryption of first 6 digits? ( ******111)

    private String occupation;          // Can be an ENUM of common occupations


    // Some Fields with default values
    public CmsCustomerProfile(String username, String givenName, String familyName, String middleName, String email) {
        // validate required fields in service layer
        super(username, givenName, familyName, middleName, email, false);
        this.preferredName = getDefaultPreferredName();
        this.picture = "defaultPicture";
        this.isEmailVerified = false;
        this.gender = null;
        this.birthDate = null;
        this.zoneInfo = ZoneInfo.EST;
        this.locale = CustomLocale.EN_CA;
        this.phoneNumber = null;
        this.isPhoneVerified = false;
        this.address = null;
        this.SIN = null;
        this.occupation = null;
    }

    // Used by builder class
    public CmsCustomerProfile(String username, String givenName, String familyName, String middleName, String preferredName,
                                String picture, String email, boolean isEmailVerified, Gender gender, Date birthDate,
                              ZoneInfo zoneInfo, CustomLocale locale, String phoneNumber, boolean isPhoneVerified,
                              String address, String sin, String occupation, boolean isProfileComplete) {
        super(username, givenName, familyName, middleName, email, isProfileComplete);
        /*Assert.isTrue(gender != null &&
                birthDate != null &&
                zoneInfo != null &&
                phoneNumber != null &&
                address != null &&
                sin != null,
                "Cannot pass null or empty values to constructor");
*/
        // other field-specific validations in service layer
        this.preferredName = (preferredName == null ? getDefaultPreferredName() : preferredName);
        this.picture = (picture == null ? "defaultPicture" : picture);
        this.isEmailVerified = isEmailVerified;
        this.gender = gender;
        this.birthDate = birthDate;
        this.zoneInfo = zoneInfo == null ? ZoneInfo.EST : zoneInfo;
        this.locale = locale == null ? CustomLocale.EN_CA : locale;
        this.phoneNumber = phoneNumber;
        this.isPhoneVerified = isPhoneVerified;
        this.address = address;
        this.SIN = sin;
        this.occupation = (occupation == null ? "" : occupation);
    }

    private String getDefaultPreferredName() {
        String middleName = getMiddleName();
        return new StringBuilder()
                .append(getGivenName())
                .append(middleName != null && !middleName.isEmpty() ? (" " + middleName + " ") : " ")
                .append(getFamilyName())
                .toString();
    }

    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        isEmailVerified = emailVerified;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public ZoneInfo getZoneInfo() {
        return zoneInfo;
    }

    public void setZoneInfo(ZoneInfo zoneInfo) {
        this.zoneInfo = zoneInfo;
    }

    public CustomLocale getLocale() {
        return locale;
    }

    public void setLocale(CustomLocale locale) {
        this.locale = locale;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isPhoneVerified() {
        return isPhoneVerified;
    }

    public void setPhoneVerified(boolean phoneVerified) {
        isPhoneVerified = phoneVerified;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSIN() {
        return SIN;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public static CmsCustomerProfile.UserProfileBuilder builder() {
        return new CmsCustomerProfile.UserProfileBuilder();
    }

    @Override
    public String toString() {
        return "CmsCustomerProfile{" +
                "username='" + getUsername() + '\'' +
                ", givenName='" + getGivenName() + '\'' +
                ", familyName='" + getFamilyName() + '\'' +
                ", middleName='" + getMiddleName() + '\'' +
                ", preferredName='" + preferredName + '\'' +
                ", picture='" + picture + '\'' +
                ", email='" + getEmail() + '\'' +
                ", isEmailVerified=" + isEmailVerified +
                ", gender=" + gender +
                ", birthDate=" + birthDate +
                ", zoneInfo=" + zoneInfo +
                ", locale=" + locale +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", isPhoneVerified=" + isPhoneVerified +
                ", address=" + address.toString() +
                ", SIN='" + SIN + '\'' +
                ", occupation='" + occupation + '\'' +
                ", isProfileComplete='" + isProfileComplete() + '\'' +
                '}';
    }

    public static final class Address {
        private String street;
        private String apartmentOrUnit;
        private String city;
        private String province;
        private String postalCode;

        public Address(String street, String apartmentOrUnit, String city, String province, String postalCode) {
            Assert.isTrue(street != null &&
                            city != null &&
                            province != null &&
                            postalCode!= null,
                    "Cannot pass null values to constructor");
            this.street = street;
            this.apartmentOrUnit = (apartmentOrUnit == null ? "" : apartmentOrUnit);
            this.city = city;
            this.province = province;
            this.postalCode = postalCode;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getApartmentOrUnit() {
            return apartmentOrUnit;
        }

        public void setApartmentOrUnit(String apartmentOrUnit) {
            this.apartmentOrUnit = apartmentOrUnit;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = postalCode;
        }

        @Override
        public String toString() {
            return (apartmentOrUnit != null && !apartmentOrUnit.equals("") ?
                    apartmentOrUnit + "-" : "") +
                    street + ", " + city + ", " + province + " " + postalCode;
        }
    }

    public static final class UserProfileBuilder {

        private String username;
        private String givenName;
        private String familyName;
        private String middleName;
        private String preferredName;
        private String picture;
        private String email;
        private boolean isEmailVerified;
        private Gender gender;
        private Date birthDate;
        private ZoneInfo zoneInfo;          // make user select the zone; retrieving from user-agent can vary the time_zone
        private CustomLocale locale;            // make user select the locale (language preferences) - English or French
        private String phoneNumber;
        private boolean isPhoneVerified;
        private String address;

        private String SIN;

        private String occupation;

        private boolean isProfileComplete;

        private UserProfileBuilder() {
        }

        public CmsCustomerProfile.UserProfileBuilder username(String username) {
            Assert.notNull(username, "username cannot be null");
            this.username = username;
            return this;
        }

        public CmsCustomerProfile.UserProfileBuilder givenName(String givenName) {
            Assert.notNull(givenName, "given name cannot be null");
            this.givenName = givenName;
            return this;
        }

        public CmsCustomerProfile.UserProfileBuilder familyName(String familyName) {
            Assert.notNull(familyName, "family name cannot be null");
            this.familyName = familyName;
            return this;
        }

        public CmsCustomerProfile.UserProfileBuilder middleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        public CmsCustomerProfile.UserProfileBuilder preferredName(String preferredName) {
            this.preferredName = preferredName;
            return this;
        }

        public CmsCustomerProfile.UserProfileBuilder picture(String picture) {
            this.picture = picture;
            return this;
        }

        public CmsCustomerProfile.UserProfileBuilder email(String email) {
            Assert.notNull(email, "email cannot be null");
            this.email = email;
            return this;
        }

        public CmsCustomerProfile.UserProfileBuilder isEmailVerified(boolean isEmailVerified) {
            this.isEmailVerified = isEmailVerified;
            return this;
        }

        public CmsCustomerProfile.UserProfileBuilder gender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public CmsCustomerProfile.UserProfileBuilder birthDate(Date birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public CmsCustomerProfile.UserProfileBuilder zoneInfo(ZoneInfo zoneInfo) {
            this.zoneInfo = zoneInfo;
            return this;
        }

        public CmsCustomerProfile.UserProfileBuilder locale(CustomLocale locale) {
            this.locale = locale;
            return this;
        }

        public CmsCustomerProfile.UserProfileBuilder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public CmsCustomerProfile.UserProfileBuilder isPhoneVerified(boolean isPhoneVerified) {
            this.isPhoneVerified = isPhoneVerified;
            return this;
        }

        public CmsCustomerProfile.UserProfileBuilder address(String address) {
            this.address = address;
            return this;
        }

        public CmsCustomerProfile.UserProfileBuilder sin(String sin) {
            this.SIN = sin;
            return this;
        }

        public CmsCustomerProfile.UserProfileBuilder occupation(String occupation) {
            this.occupation = occupation;
            return this;
        }

        public CmsCustomerProfile.UserProfileBuilder isProfileComplete(boolean isProfileComplete) {
            this.isProfileComplete = isProfileComplete;
            return this;
        }

        public CmsCustomerProfile build() {
            return new CmsCustomerProfile(this.username, this.givenName, this.familyName, this.middleName, this.preferredName,
                    this.picture, this.email, this.isEmailVerified, this.gender, this.birthDate, this.zoneInfo,
                    this.locale, this.phoneNumber, this.isPhoneVerified, this.address, this.SIN, this.occupation,
                    this.isProfileComplete);
        }
    }
}
