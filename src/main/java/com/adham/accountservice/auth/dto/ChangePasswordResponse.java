package com.adham.accountservice.auth.dto;

import com.adham.accountservice.auth.model.UserAccount;

public class ChangePasswordResponse {

    private static final String SUCCESS_MESSAGE = "The password has been updated successfully";

    private final String email;
    private final String status;

    public ChangePasswordResponse(String email, String status) {
        this.email = email;
        this.status = status;
    }

    public static ChangePasswordResponse from(UserAccount user) {
        return new ChangePasswordResponse(user.getEmail(), SUCCESS_MESSAGE);
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }
}
