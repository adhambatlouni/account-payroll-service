package com.adham.accountservice;

import com.adham.accountservice.audit.model.SecurityEventAction;
import com.adham.accountservice.audit.repository.SecurityEventRepository;
import com.adham.accountservice.auth.model.Role;
import com.adham.accountservice.auth.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:bootstrap-admin-test;DB_CLOSE_DELAY=-1",
        "app.bootstrap-admin.enabled=true",
        "app.bootstrap-admin.name=Portfolio",
        "app.bootstrap-admin.lastname=Administrator",
        "app.bootstrap-admin.email=BOOTSTRAP@ACME.COM",
        "app.bootstrap-admin.password=BootstrapPassword123"
})
@ActiveProfiles("test")
class AdminBootstrapIntegrationTests {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private SecurityEventRepository securityEventRepository;

    @Test
    void createsConfiguredAdministratorAndAuditEvent() {
        var administrator = userAccountRepository
                .findByEmailIgnoreCase("bootstrap@acme.com")
                .orElseThrow();

        assertThat(administrator.hasRole(Role.ROLE_ADMINISTRATOR)).isTrue();
        assertThat(securityEventRepository.findAllByOrderByIdAsc())
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.getAction()).isEqualTo(SecurityEventAction.CREATE_USER);
                    assertThat(event.getSubject()).isEqualTo("System");
                    assertThat(event.getObject()).isEqualTo("bootstrap@acme.com");
                });
    }
}
