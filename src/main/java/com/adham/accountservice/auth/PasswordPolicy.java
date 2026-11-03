package com.adham.accountservice.auth;

import com.adham.accountservice.auth.exception.BreachedPasswordException;
import com.adham.accountservice.auth.exception.PasswordLengthException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PasswordPolicy {

    private static final int MINIMUM_LENGTH = 12;

    private static final Set<String> BREACHED_PASSWORDS = Set.of(
            "PasswordForJanuary",
            "PasswordForFebruary",
            "PasswordForMarch",
            "PasswordForApril",
            "PasswordForMay",
            "PasswordForJune",
            "PasswordForJuly",
            "PasswordForAugust",
            "PasswordForSeptember",
            "PasswordForOctober",
            "PasswordForNovember",
            "PasswordForDecember"
    );

    public void validate(String password) {
        if (password == null || password.length() < MINIMUM_LENGTH) {
            throw new PasswordLengthException();
        }
        if (BREACHED_PASSWORDS.contains(password)) {
            throw new BreachedPasswordException();
        }
    }
}
