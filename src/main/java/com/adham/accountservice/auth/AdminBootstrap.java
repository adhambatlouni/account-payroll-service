package com.adham.accountservice.auth;

import com.adham.accountservice.audit.AuditService;
import com.adham.accountservice.audit.model.SecurityEventAction;
import com.adham.accountservice.auth.model.Role;
import com.adham.accountservice.auth.model.UserAccount;
import com.adham.accountservice.auth.repository.UserAccountRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private final BootstrapAdminProperties properties;
    private final UserAccountRepository userRepository;
    private final PasswordPolicy passwordPolicy;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AdminBootstrap(
            BootstrapAdminProperties properties,
            UserAccountRepository userRepository,
            PasswordPolicy passwordPolicy,
            PasswordEncoder passwordEncoder,
            AuditService auditService
    ) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.passwordPolicy = passwordPolicy;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        if (!properties.enabled() || userRepository.existsByRole(Role.ROLE_ADMINISTRATOR)) {
            return;
        }

        validateConfiguration();
        String email = properties.email().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException(
                    "Bootstrap administrator email is already assigned to a non-administrator account"
            );
        }

        passwordPolicy.validate(properties.password());
        UserAccount administrator = userRepository.saveAndFlush(new UserAccount(
                properties.name(),
                properties.lastname(),
                email,
                passwordEncoder.encode(properties.password()),
                Role.ROLE_ADMINISTRATOR
        ));
        auditService.log(
                SecurityEventAction.CREATE_USER,
                "System",
                administrator.getEmail(),
                "/bootstrap"
        );
    }

    private void validateConfiguration() {
        if (isBlank(properties.name())
                || isBlank(properties.lastname())
                || isBlank(properties.email())
                || isBlank(properties.password())) {
            throw new IllegalStateException(
                    "Administrator bootstrap is enabled but its name, lastname, email, or password is missing"
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
