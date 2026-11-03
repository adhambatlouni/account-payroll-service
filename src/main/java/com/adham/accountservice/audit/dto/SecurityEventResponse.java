package com.adham.accountservice.audit.dto;

import com.adham.accountservice.audit.model.SecurityEvent;
import com.adham.accountservice.audit.model.SecurityEventAction;

import java.time.LocalDate;

public class SecurityEventResponse {

    private final Long id;
    private final LocalDate date;
    private final SecurityEventAction action;
    private final String subject;
    private final String object;
    private final String path;

    public SecurityEventResponse(SecurityEvent event) {
        this.id = event.getId();
        this.date = event.getDate();
        this.action = event.getAction();
        this.subject = event.getSubject();
        this.object = event.getObject();
        this.path = event.getPath();
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public SecurityEventAction getAction() {
        return action;
    }

    public String getSubject() {
        return subject;
    }

    public String getObject() {
        return object;
    }

    public String getPath() {
        return path;
    }
}
