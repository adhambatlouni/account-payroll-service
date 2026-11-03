package com.adham.accountservice.payment.dto;

public class OperationStatusResponse {

    private final String status;

    public OperationStatusResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
