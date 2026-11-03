package com.adham.accountservice.admin.dto;

public class DeleteUserResponse {

    private final String user;
    private final String status;

    public DeleteUserResponse(String user) {
        this.user = user;
        this.status = "Deleted successfully!";
    }

    public String getUser() {
        return user;
    }

    public String getStatus() {
        return status;
    }
}
