package com.example.shdauthorizationserver.model;

public enum RegisteredClientStore {
    CLIENT_9DFD919F17AD2C97C24E543C3F954DD3("9DFD919F17AD2C97C24E543C3F954DD3", "Credit Management System",
            "CMS", "https://placeholder");

    public final String id;
    public final String cname; // company/application public name

    public final String aname; // company/application acronym
    public final String logoUrl;


    RegisteredClientStore(String id, String cname, String aname, String logoUrl) {
        this.id = id;
        this.cname = cname;
        this.aname = aname;
        this.logoUrl = logoUrl;
    }
}
