package com.example.shdauthorizationserver.model;

import java.util.Locale;

public enum CustomLocale {
    EN_CA(new Locale("en", "CA")),
    FR_CA(new Locale("fr", "CA"));

    public final Locale locale;

    private CustomLocale(Locale locale) {
        this.locale = locale;
    }
}
