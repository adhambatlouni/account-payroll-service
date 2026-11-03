package com.adham.accountservice.auth;

import com.adham.accountservice.auth.dto.ChangePasswordRequest;
import com.adham.accountservice.auth.dto.ChangePasswordResponse;
import com.adham.accountservice.auth.dto.SignupRequest;
import com.adham.accountservice.auth.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Account registration and password management")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    @Operation(summary = "Register a user account")
    public ResponseEntity<UserResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(UserResponse.from(userService.register(request)));
    }

    @PostMapping("/changepass")
    @Operation(
            summary = "Change the authenticated user's password",
            security = @SecurityRequirement(name = "basicAuth")
    )
    public ResponseEntity<ChangePasswordResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ChangePasswordResponse.from(
                userService.changePassword(authentication.getName(), request.getNewPassword())
        ));
    }
}
