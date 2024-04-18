package com.example.shdauthorizationserver.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CustomerRegistrationRequestDto {
    @NotBlank(message = "Username is required")
    @Size(max = 30, message = "Username must be less than or equal to 30 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(max = 20, message = "Password must be less than or equal to 20 characters")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@*^#!&]).{8,20}$",
            message = """
                    Password must be between 8-20 characters with at least
                    one digit, one lowercase letter, one uppercase letter,
                    and one special character @*^#!&
                    """)
    private String password;

    @NotBlank(message = "Given Name is required")
    @Size(max = 100, message = "Given Name must be less than or equal to 100 characters")
    private String givenName;

    @NotBlank(message = "Family Name is required")
    @Size(max = 100, message = "Family Name must be less than or equal to 100 characters")
    private String familyName;

    @Size(max = 100, message = "Middle Name must be less than or equal to 100 characters")
    private String middleName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 254, message = "Email must be less than or equal to 254 characters")
    private String email;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
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
}
