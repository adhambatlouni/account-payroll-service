package com.adham.accountservice.security;

import com.adham.accountservice.audit.AuditService;
import com.adham.accountservice.audit.model.SecurityEventAction;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final AuditService auditService;
    private final SecurityErrorWriter errorWriter;

    public RestAccessDeniedHandler(
            AuditService auditService,
            SecurityErrorWriter errorWriter
    ) {
        this.auditService = auditService;
        this.errorWriter = errorWriter;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String subject = authentication == null ? "Anonymous" : authentication.getName();
        String path = normalizePath(request.getRequestURI());
        auditService.log(SecurityEventAction.ACCESS_DENIED, subject, path, path);
        errorWriter.write(
                request,
                response,
                HttpServletResponse.SC_FORBIDDEN,
                "Access denied",
                "You do not have permission to access this resource.",
                "ACCESS_DENIED"
        );
    }

    private String normalizePath(String path) {
        return path.length() > 1 && path.endsWith("/")
                ? path.substring(0, path.length() - 1)
                : path;
    }
}
