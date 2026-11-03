package com.adham.accountservice.auth.dto;

import com.adham.accountservice.auth.model.Role;
import com.adham.accountservice.auth.model.UserAccount;

import java.util.List;

public class UserResponse {

    private final Long id;
    private final String name;
    private final String lastname;
    private final String email;
    private final List<String> roles;

    public UserResponse(Long id, String name, String lastname, String email, List<String> roles) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.roles = roles;
    }

    public static UserResponse from(UserAccount user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getLastname(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(Role::name)
                        .sorted()
                        .toList()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }
}
