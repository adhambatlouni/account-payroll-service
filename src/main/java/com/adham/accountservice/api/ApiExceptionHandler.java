package com.adham.accountservice.api;

import com.adham.accountservice.auth.exception.BreachedPasswordException;
import com.adham.accountservice.auth.exception.PasswordLengthException;
import com.adham.accountservice.auth.exception.SamePasswordException;
import com.adham.accountservice.auth.exception.UserExistException;
import com.adham.accountservice.payment.exception.PayrollOperationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UserExistException.class)
    public ProblemDetail handleUserExists(
            UserExistException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "User already exists",
                exception.getMessage(),
                "USER_ALREADY_EXISTS",
                request
        );
    }

    @ExceptionHandler(PasswordLengthException.class)
    public ProblemDetail handlePasswordLength(
            PasswordLengthException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Password rejected",
                exception.getMessage(),
                "PASSWORD_TOO_SHORT",
                request
        );
    }

    @ExceptionHandler(BreachedPasswordException.class)
    public ProblemDetail handleBreachedPassword(
            BreachedPasswordException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Password rejected",
                exception.getMessage(),
                "PASSWORD_COMPROMISED",
                request
        );
    }

    @ExceptionHandler(SamePasswordException.class)
    public ProblemDetail handleSamePassword(
            SamePasswordException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Password rejected",
                exception.getMessage(),
                "PASSWORD_UNCHANGED",
                request
        );
    }

    @ExceptionHandler(PayrollOperationException.class)
    public ProblemDetail handlePayrollOperation(
            PayrollOperationException exception,
            HttpServletRequest request
    ) {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Payroll operation failed",
                exception.getMessage(),
                "PAYROLL_OPERATION_FAILED",
                request
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatus(
            ResponseStatusException exception,
            HttpServletRequest request
    ) {
        String detail = exception.getReason() == null
                ? "The request could not be completed."
                : exception.getReason();
        return problem(
                exception.getStatusCode(),
                "Request rejected",
                detail,
                "REQUEST_REJECTED",
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<Map<String, String>> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> {
                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("field", error.getField());
                    item.put("message", error.getDefaultMessage());
                    return item;
                })
                .toList();

        ProblemDetail problem = problem(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                "One or more request fields are invalid.",
                "VALIDATION_FAILED",
                request
        );
        problem.setProperty("errors", errors);
        return problem;
    }

    private ProblemDetail problem(
            HttpStatusCode status,
            String title,
            String detail,
            String code,
            HttpServletRequest request
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(normalizePath(request.getRequestURI())));
        problem.setProperty("code", code);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    private String normalizePath(String path) {
        return path.length() > 1 && path.endsWith("/")
                ? path.substring(0, path.length() - 1)
                : path;
    }
}
