package com.adham.accountservice.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class PayrollOperationException extends RuntimeException {

    public PayrollOperationException(String message) {
        super(message);
    }
}
