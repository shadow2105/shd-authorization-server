package com.example.shdauthorizationserver.model;

public enum ZoneInfo {
    PST("Pacific Standard Time (PST) - UTC-8 hours"),
    MST("Mountain Standard Time (MST) - UTC-7 hours"),
    CST("Central Standard Time (CST) - UTC-6 hours"),
    EST("Eastern Standard Time (EST) - UTC-5 hours"),
    AST("Atlantic Standard Time (AST) - UTC-4 hours"),
    NST("Newfoundland Standard Time (NST) - UTC-3:30 hours");

    public final String label;

    private ZoneInfo(String label) {
        this.label = label;
    }
}
