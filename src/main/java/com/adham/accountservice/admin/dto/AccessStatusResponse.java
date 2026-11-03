package com.adham.accountservice.admin.dto;

public class AccessStatusResponse {

    private final String status;

    public AccessStatusResponse(String email, String state) {
        this.status = "User " + email + " " + state + "!";
    }

    public String getStatus() {
        return status;
    }
}
