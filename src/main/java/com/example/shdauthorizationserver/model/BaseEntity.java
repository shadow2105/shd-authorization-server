package com.example.shdauthorizationserver.model;

import java.io.Serializable;
import java.util.UUID;

public abstract class BaseEntity implements Serializable {

    private UUID id;

    public BaseEntity() {
    }

    public BaseEntity(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }
}
