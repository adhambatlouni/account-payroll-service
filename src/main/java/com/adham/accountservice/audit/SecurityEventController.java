package com.adham.accountservice.audit;

import com.adham.accountservice.audit.dto.SecurityEventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/security/events")
@Tag(name = "Security audit", description = "Persistent security-event history")
@SecurityRequirement(name = "basicAuth")
public class SecurityEventController {

    private final AuditService auditService;

    public SecurityEventController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @Operation(summary = "List security audit events")
    public ResponseEntity<List<SecurityEventResponse>> getEvents() {
        return ResponseEntity.ok(auditService.getAll());
    }
}
