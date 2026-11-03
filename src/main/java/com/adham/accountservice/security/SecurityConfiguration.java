package com.adham.accountservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
public class SecurityConfiguration {

    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;
    private final LoginSuccessFilter loginSuccessFilter;

    public SecurityConfiguration(
            RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            RestAccessDeniedHandler restAccessDeniedHandler,
            LoginSuccessFilter loginSuccessFilter
    ) {
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
        this.loginSuccessFilter = loginSuccessFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(basic -> basic
                        .authenticationEntryPoint(restAuthenticationEntryPoint))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/signup").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/changepass")
                        .access(new WebExpressionAuthorizationManager(
                                "hasAnyRole('USER','ACCOUNTANT','ADMINISTRATOR') and !hasRole('AUDITOR')"
                        ))
                        .requestMatchers(HttpMethod.GET, "/api/empl/payment")
                        .access(new WebExpressionAuthorizationManager(
                                "hasAnyRole('USER','ACCOUNTANT') and !hasRole('AUDITOR')"
                        ))
                        .requestMatchers(HttpMethod.POST, "/api/acct/payments", "/api/acct/payments/")
                        .access(new WebExpressionAuthorizationManager(
                                "hasRole('ACCOUNTANT') and !hasRole('AUDITOR')"
                        ))
                        .requestMatchers(HttpMethod.PUT, "/api/acct/payments", "/api/acct/payments/")
                        .access(new WebExpressionAuthorizationManager(
                                "hasRole('ACCOUNTANT') and !hasRole('AUDITOR')"
                        ))
                        .requestMatchers("/api/admin/**")
                        .hasAuthority("ROLE_ADMINISTRATOR")
                        .requestMatchers("/api/security/events")
                        .hasAuthority("ROLE_AUDITOR")
                        .anyRequest().denyAll())
                .sessionManagement(sessions -> sessions
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAfter(loginSuccessFilter, BasicAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(13);
    }
}
