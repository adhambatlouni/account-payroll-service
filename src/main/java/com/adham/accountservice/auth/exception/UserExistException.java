package com.adham.accountservice.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "User exist!")
public class UserExistException extends RuntimeException {

    public UserExistException() {
        super("User exist!");
    }
}
