package com.adham.accountservice.auth.model;

public enum Role {
    ROLE_ACCOUNTANT,
    ROLE_ADMINISTRATOR,
    ROLE_AUDITOR,
    ROLE_USER;

    public boolean isAdministrative() {
        return this == ROLE_ADMINISTRATOR;
    }
}
