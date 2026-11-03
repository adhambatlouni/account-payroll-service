package com.adham.accountservice.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

@Component
public class SecurityErrorWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            int statusCode,
            String title,
            String detail,
            String code
    ) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.valueOf(statusCode),
                detail
        );
        problem.setTitle(title);
        problem.setInstance(URI.create(normalizePath(request.getRequestURI())));
        problem.setProperty("code", code);
        problem.setProperty("timestamp", Instant.now());

        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }

    private String normalizePath(String path) {
        return path.length() > 1 && path.endsWith("/")
                ? path.substring(0, path.length() - 1)
                : path;
    }
}
