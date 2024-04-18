package com.example.shdauthorizationserver.model;

public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    public final String label;

    private Gender(String label) {
        this.label = label;
    }
}
