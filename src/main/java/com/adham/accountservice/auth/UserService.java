package com.adham.accountservice.auth;

import com.adham.accountservice.audit.AuditService;
import com.adham.accountservice.audit.model.SecurityEventAction;
import com.adham.accountservice.auth.dto.SignupRequest;
import com.adham.accountservice.auth.exception.SamePasswordException;
import com.adham.accountservice.auth.exception.UserExistException;
import com.adham.accountservice.auth.model.Role;
import com.adham.accountservice.auth.model.UserAccount;
import com.adham.accountservice.auth.repository.UserAccountRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserService implements UserDetailsService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final AuditService auditService;

    public UserService(
            UserAccountRepository userRepository,
            PasswordEncoder passwordEncoder,
            PasswordPolicy passwordPolicy,
            AuditService auditService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = passwordPolicy;
        this.auditService = auditService;
    }

    @Transactional
    public UserAccount register(SignupRequest request) {
        passwordPolicy.validate(request.getPassword());
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new UserExistException();
        }

        UserAccount user = new UserAccount(
                request.getName(),
                request.getLastname(),
                email,
                passwordEncoder.encode(request.getPassword()),
                Role.ROLE_USER
        );

        try {
            UserAccount savedUser = userRepository.saveAndFlush(user);
            auditService.log(
                    SecurityEventAction.CREATE_USER,
                    "Anonymous",
                    savedUser.getEmail(),
                    "/api/auth/signup"
            );
            return savedUser;
        } catch (DataIntegrityViolationException exception) {
            throw new UserExistException();
        }
    }

    @Transactional
    public UserAccount changePassword(String email, String newPassword) {
        UserAccount user = findByEmail(email);
        passwordPolicy.validate(newPassword);

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new SamePasswordException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        auditService.log(
                SecurityEventAction.CHANGE_PASSWORD,
                user.getEmail(),
                user.getEmail(),
                "/api/auth/changepass"
        );
        return user;
    }

    @Transactional(readOnly = true)
    public UserAccount findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount user = findByEmail(username);
        return User.withUsername(user.getEmail())
                .password(user.getPassword())
                .accountLocked(user.isLocked())
                .authorities(user.getRoles().stream()
                        .map(Role::name)
                        .toArray(String[]::new))
                .build();
    }

    private String normalizeEmail(String email) {
        return email.toLowerCase(Locale.ROOT);
    }
}
