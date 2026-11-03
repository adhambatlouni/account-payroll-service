package com.adham.accountservice.admin;

import com.adham.accountservice.audit.AuditService;
import com.adham.accountservice.audit.model.SecurityEventAction;
import com.adham.accountservice.admin.dto.AccessChangeRequest;
import com.adham.accountservice.admin.dto.AccessStatusResponse;
import com.adham.accountservice.admin.dto.DeleteUserResponse;
import com.adham.accountservice.admin.dto.RoleChangeRequest;
import com.adham.accountservice.auth.dto.UserResponse;
import com.adham.accountservice.auth.model.Role;
import com.adham.accountservice.auth.model.UserAccount;
import com.adham.accountservice.auth.repository.UserAccountRepository;
import com.adham.accountservice.payment.repository.PayrollRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class AdminService {

    private final UserAccountRepository userRepository;
    private final PayrollRepository payrollRepository;
    private final AuditService auditService;

    public AdminService(
            UserAccountRepository userRepository,
            PayrollRepository payrollRepository,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.payrollRepository = payrollRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        return userRepository.findAllByOrderByIdAsc().stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public DeleteUserResponse deleteUser(String email, String actor) {
        UserAccount user = findUser(email);
        if (user.hasRole(Role.ROLE_ADMINISTRATOR)) {
            throw badRequest("Can't remove ADMINISTRATOR role!");
        }

        payrollRepository.deleteByEmployee(user);
        userRepository.delete(user);
        auditService.log(
                SecurityEventAction.DELETE_USER,
                actor,
                user.getEmail(),
                "/api/admin/user"
        );
        return new DeleteUserResponse(user.getEmail());
    }

    @Transactional
    public UserResponse changeRole(RoleChangeRequest request, String actor) {
        UserAccount user = findUser(request.getUser());
        Role role = parseRole(request.getRole());

        if ("GRANT".equalsIgnoreCase(request.getOperation())) {
            grantRole(user, role);
            auditService.log(
                    SecurityEventAction.GRANT_ROLE,
                    actor,
                    "Grant role " + roleName(role) + " to " + user.getEmail(),
                    "/api/admin/user/role"
            );
        } else if ("REMOVE".equalsIgnoreCase(request.getOperation())) {
            removeRole(user, role);
            auditService.log(
                    SecurityEventAction.REMOVE_ROLE,
                    actor,
                    "Remove role " + roleName(role) + " from " + user.getEmail(),
                    "/api/admin/user/role"
            );
        } else {
            throw badRequest("Unknown operation!");
        }

        return UserResponse.from(user);
    }

    @Transactional
    public AccessStatusResponse changeAccess(AccessChangeRequest request, String actor) {
        UserAccount user = findUser(request.getUser());
        String operation = request.getOperation().toUpperCase(Locale.ROOT);

        if ("LOCK".equals(operation)) {
            if (user.hasRole(Role.ROLE_ADMINISTRATOR)) {
                throw badRequest("Can't lock the ADMINISTRATOR!");
            }
            user.setLocked(true);
            auditService.log(
                    SecurityEventAction.LOCK_USER,
                    actor,
                    "Lock user " + user.getEmail(),
                    "/api/admin/user/access"
            );
            return new AccessStatusResponse(user.getEmail(), "locked");
        }

        if ("UNLOCK".equals(operation)) {
            user.setLocked(false);
            user.resetFailedAttempts();
            auditService.log(
                    SecurityEventAction.UNLOCK_USER,
                    actor,
                    "Unlock user " + user.getEmail(),
                    "/api/admin/user/access"
            );
            return new AccessStatusResponse(user.getEmail(), "unlocked");
        }

        throw badRequest("Unknown operation!");
    }

    private void grantRole(UserAccount user, Role role) {
        boolean mixesRoleGroups = user.getRoles().stream()
                .anyMatch(existingRole -> existingRole.isAdministrative() != role.isAdministrative());
        if (mixesRoleGroups) {
            throw badRequest("The user cannot combine administrative and business roles!");
        }
        user.grantRole(role);
    }

    private void removeRole(UserAccount user, Role role) {
        if (!user.hasRole(role)) {
            throw badRequest("The user does not have a role!");
        }
        if (role == Role.ROLE_ADMINISTRATOR) {
            throw badRequest("Can't remove ADMINISTRATOR role!");
        }
        if (user.roleCount() == 1) {
            throw badRequest("The user must have at least one role!");
        }
        user.removeRole(role);
    }

    private UserAccount findUser(String email) {
        return userRepository.findByEmailIgnoreCase(email.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found!"
                ));
    }

    private Role parseRole(String role) {
        try {
            return Role.valueOf("ROLE_" + role.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found!");
        }
    }

    private String roleName(Role role) {
        return role.name().substring("ROLE_".length());
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
