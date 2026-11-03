package com.adham.accountservice.admin;

import com.adham.accountservice.admin.dto.AccessChangeRequest;
import com.adham.accountservice.admin.dto.AccessStatusResponse;
import com.adham.accountservice.admin.dto.DeleteUserResponse;
import com.adham.accountservice.admin.dto.RoleChangeRequest;
import com.adham.accountservice.auth.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/user")
@Tag(name = "Administration", description = "User, role, and account-access administration")
@SecurityRequirement(name = "basicAuth")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    @Operation(summary = "List all user accounts")
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(adminService.getUsers());
    }

    @DeleteMapping("/{email}")
    @Operation(summary = "Delete a user account")
    public ResponseEntity<DeleteUserResponse> deleteUser(
            @PathVariable String email,
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminService.deleteUser(email, authentication.getName()));
    }

    @PutMapping("/role")
    @Operation(summary = "Grant or remove a user role")
    public ResponseEntity<UserResponse> changeRole(
            @Valid @RequestBody RoleChangeRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminService.changeRole(request, authentication.getName()));
    }

    @PutMapping("/access")
    @Operation(summary = "Lock or unlock a user account")
    public ResponseEntity<AccessStatusResponse> changeAccess(
            @Valid @RequestBody AccessChangeRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(adminService.changeAccess(request, authentication.getName()));
    }
}
