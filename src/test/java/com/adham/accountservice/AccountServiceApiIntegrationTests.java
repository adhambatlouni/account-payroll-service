package com.adham.accountservice;

import com.adham.accountservice.audit.repository.SecurityEventRepository;
import com.adham.accountservice.auth.model.Role;
import com.adham.accountservice.auth.model.UserAccount;
import com.adham.accountservice.auth.repository.UserAccountRepository;
import com.adham.accountservice.payment.repository.PayrollRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class AccountServiceApiIntegrationTests {

    private static final String ADMIN_EMAIL = "admin@acme.com";
    private static final String ADMIN_PASSWORD = "AdminPassword123";
    private static final String USER_PASSWORD = "EmployeePassword123";

    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private SecurityEventRepository securityEventRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        securityEventRepository.deleteAll();
        payrollRepository.deleteAll();
        userAccountRepository.deleteAll();
        userAccountRepository.saveAndFlush(new UserAccount(
                "Admin",
                "Owner",
                ADMIN_EMAIL,
                passwordEncoder.encode(ADMIN_PASSWORD),
                Role.ROLE_ADMINISTRATOR
        ));
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void registrationNormalizesEmailAndReturnsStructuredErrors() throws Exception {
        signup("New", "User", "NEW.USER@ACME.COM", USER_PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new.user@acme.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        signup("Other", "User", "new.user@acme.com", USER_PASSWORD)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("USER_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.instance").value("/api/auth/signup"));

        signup("Weak", "Password", "weak@acme.com", "short")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PASSWORD_TOO_SHORT"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Invalid",
                                  "lastname": "Email",
                                  "email": "invalid@example.com",
                                  "password": "ValidPassword123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("email"));
    }

    @Test
    void rolePayrollAndAuditWorkflowEnforcesAuthorization() throws Exception {
        signup("Emily", "Stone", "employee@acme.com", USER_PASSWORD)
                .andExpect(status().isOk());
        signup("Alex", "Ledger", "accountant@acme.com", USER_PASSWORD)
                .andExpect(status().isOk());
        signup("Ava", "Audit", "auditor@acme.com", USER_PASSWORD)
                .andExpect(status().isOk());

        changeRole("accountant@acme.com", "ACCOUNTANT", "GRANT")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ROLE_ACCOUNTANT"))
                .andExpect(jsonPath("$.roles[1]").value("ROLE_USER"));
        changeRole("auditor@acme.com", "AUDITOR", "GRANT")
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/user")
                        .with(httpBasic("employee@acme.com", USER_PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        mockMvc.perform(post("/api/acct/payments")
                        .with(httpBasic("accountant@acme.com", USER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {
                                    "employee": "employee@acme.com",
                                    "period": "10-2026",
                                    "salary": 250075
                                  }
                                ]
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Added successfully!"));

        mockMvc.perform(get("/api/empl/payment")
                        .param("period", "10-2026")
                        .with(httpBasic("employee@acme.com", USER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Emily"))
                .andExpect(jsonPath("$.period").value("October-2026"))
                .andExpect(jsonPath("$.salary").value("2500 dollar(s) 75 cent(s)"));

        mockMvc.perform(get("/api/security/events")
                        .with(httpBasic("auditor@acme.com", USER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("CREATE_USER"))
                .andExpect(jsonPath("$[?(@.action == 'GRANT_ROLE')]").isNotEmpty())
                .andExpect(jsonPath("$[?(@.action == 'ACCESS_DENIED')]").isNotEmpty());

        mockMvc.perform(get("/api/admin/user"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void fiveFailedLoginsLockNonAdministratorAccount() throws Exception {
        signup("Emily", "Stone", "employee@acme.com", USER_PASSWORD)
                .andExpect(status().isOk());

        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(get("/api/empl/payment")
                            .with(httpBasic("employee@acme.com", "WrongPassword123")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
        }

        mockMvc.perform(get("/api/empl/payment")
                        .with(httpBasic("employee@acme.com", USER_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail")
                        .value("Authentication is required or the supplied credentials are invalid."));

        mockMvc.perform(get("/api/admin/user")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.email == 'employee@acme.com')]").isNotEmpty());

        mockMvc.perform(put("/api/admin/user/access")
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "user": "employee@acme.com",
                                  "operation": "UNLOCK"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/empl/payment")
                        .with(httpBasic("employee@acme.com", USER_PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    void administratorCannotBeDeleted() throws Exception {
        mockMvc.perform(delete("/api/admin/user/{email}", ADMIN_EMAIL)
                        .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUEST_REJECTED"));
    }

    @Test
    void openApiDescriptionIsPubliclyAvailable() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Account Service API"))
                .andExpect(jsonPath("$.components.securitySchemes.basicAuth.type").value("http"))
                .andExpect(jsonPath("$.paths['/api/auth/signup'].post").exists())
                .andExpect(jsonPath("$.paths['/api/acct/payments'].post.security[0].basicAuth").exists())
                .andExpect(jsonPath("$.paths['/api/admin/user/']").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/empl/payment/']").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/security/events/']").doesNotExist());
    }

    private org.springframework.test.web.servlet.ResultActions signup(
            String name,
            String lastname,
            String email,
            String password
    ) throws Exception {
        return mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "name": "%s",
                          "lastname": "%s",
                          "email": "%s",
                          "password": "%s"
                        }
                        """.formatted(name, lastname, email, password)));
    }

    private org.springframework.test.web.servlet.ResultActions changeRole(
            String user,
            String role,
            String operation
    ) throws Exception {
        return mockMvc.perform(put("/api/admin/user/role")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "user": "%s",
                          "role": "%s",
                          "operation": "%s"
                        }
                        """.formatted(user, role, operation)));
    }
}
