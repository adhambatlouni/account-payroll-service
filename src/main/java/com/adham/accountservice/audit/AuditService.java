package com.adham.accountservice.audit;

import com.adham.accountservice.audit.dto.SecurityEventResponse;
import com.adham.accountservice.audit.model.SecurityEvent;
import com.adham.accountservice.audit.model.SecurityEventAction;
import com.adham.accountservice.audit.repository.SecurityEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AuditService {

    private final SecurityEventRepository eventRepository;

    public AuditService(SecurityEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public void log(
            SecurityEventAction action,
            String subject,
            String object,
            String path
    ) {
        eventRepository.save(new SecurityEvent(
                LocalDate.now(),
                action,
                subject,
                object,
                normalizePath(path)
        ));
    }

    @Transactional(readOnly = true)
    public List<SecurityEventResponse> getAll() {
        return eventRepository.findAllByOrderByIdAsc().stream()
                .map(SecurityEventResponse::new)
                .toList();
    }

    private String normalizePath(String path) {
        if (path != null && path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
}
