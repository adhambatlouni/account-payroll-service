package com.adham.accountservice.auth;

import com.adham.accountservice.auth.exception.BreachedPasswordException;
import com.adham.accountservice.auth.exception.PasswordLengthException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyTests {

    private final PasswordPolicy passwordPolicy = new PasswordPolicy();

    @Test
    void acceptsStrongPassword() {
        assertThatCode(() -> passwordPolicy.validate("CorrectHorseBatteryStaple"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsPasswordShorterThanTwelveCharacters() {
        assertThatThrownBy(() -> passwordPolicy.validate("TooShort1"))
                .isInstanceOf(PasswordLengthException.class)
                .hasMessage("Password length must be 12 chars minimum!");
    }

    @Test
    void rejectsKnownBreachedPassword() {
        assertThatThrownBy(() -> passwordPolicy.validate("PasswordForJanuary"))
                .isInstanceOf(BreachedPasswordException.class)
                .hasMessage("The password is in the hacker's database!");
    }
}
