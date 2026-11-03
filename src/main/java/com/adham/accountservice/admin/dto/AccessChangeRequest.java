package com.adham.accountservice.admin.dto;

import jakarta.validation.constraints.NotBlank;

public class AccessChangeRequest {

    @NotBlank
    private String user;

    @NotBlank
    private String operation;

    public AccessChangeRequest() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }
}
