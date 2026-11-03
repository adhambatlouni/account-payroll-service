package com.adham.accountservice.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final LoginAttemptService loginAttemptService;
    private final SecurityErrorWriter errorWriter;

    public RestAuthenticationEntryPoint(
            LoginAttemptService loginAttemptService,
            SecurityErrorWriter errorWriter
    ) {
        this.loginAttemptService = loginAttemptService;
        this.errorWriter = errorWriter;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        String username = extractUsername(request.getHeader("Authorization"));
        if (username != null && !(authException instanceof LockedException)) {
            loginAttemptService.loginFailed(username, normalizePath(request.getRequestURI()));
        }
        errorWriter.write(
                request,
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "Authentication required",
                "Authentication is required or the supplied credentials are invalid.",
                "AUTHENTICATION_REQUIRED"
        );
    }

    private String extractUsername(String authorization) {
        if (authorization == null || !authorization.regionMatches(true, 0, "Basic ", 0, 6)) {
            return null;
        }
        try {
            String credentials = new String(
                    Base64.getDecoder().decode(authorization.substring(6)),
                    StandardCharsets.UTF_8
            );
            int separator = credentials.indexOf(':');
            if (separator <= 0) {
                return null;
            }
            return credentials.substring(0, separator).toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String normalizePath(String path) {
        return path.length() > 1 && path.endsWith("/")
                ? path.substring(0, path.length() - 1)
                : path;
    }
}
