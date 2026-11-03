package com.adham.accountservice.security;

import com.adham.accountservice.audit.AuditService;
import com.adham.accountservice.audit.model.SecurityEventAction;
import com.adham.accountservice.auth.model.Role;
import com.adham.accountservice.auth.model.UserAccount;
import com.adham.accountservice.auth.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
public class LoginAttemptService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final String ACCESS_API = "/api/admin/user/access";

    private final UserAccountRepository userRepository;
    private final AuditService auditService;

    public LoginAttemptService(
            UserAccountRepository userRepository,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void loginFailed(String suppliedEmail, String path) {
        String email = suppliedEmail.toLowerCase(Locale.ROOT);
        auditService.log(SecurityEventAction.LOGIN_FAILED, email, path, path);

        Optional<UserAccount> optionalUser = userRepository.findForUpdateByEmail(email);
        if (optionalUser.isEmpty()) {
            return;
        }

        UserAccount user = optionalUser.get();
        int attempts = user.incrementFailedAttempts();
        if (attempts >= MAX_FAILED_ATTEMPTS
                && !user.hasRole(Role.ROLE_ADMINISTRATOR)
                && !user.isLocked()) {
            user.setLocked(true);
            auditService.log(SecurityEventAction.BRUTE_FORCE, email, path, path);
            auditService.log(
                    SecurityEventAction.LOCK_USER,
                    email,
                    "Lock user " + email,
                    ACCESS_API
            );
        }
    }

    @Transactional
    public void loginSucceeded(String email) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(UserAccount::resetFailedAttempts);
    }
}
