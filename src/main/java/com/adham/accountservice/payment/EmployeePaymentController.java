package com.adham.accountservice.payment;

import com.adham.accountservice.payment.dto.PaymentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/empl/payment")
@Tag(name = "Employee payroll", description = "Authenticated employee payroll history")
@SecurityRequirement(name = "basicAuth")
public class EmployeePaymentController {

    private final PayrollService payrollService;

    public EmployeePaymentController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping
    @Operation(summary = "Get payroll history or one requested period")
    public ResponseEntity<?> getPayment(
            @RequestParam(required = false) String period,
            Authentication authentication
    ) {
        if (period == null) {
            List<PaymentResponse> payments = payrollService.getAll(authentication.getName());
            return ResponseEntity.ok(payments);
        }

        Optional<PaymentResponse> payment = payrollService.getOne(
                authentication.getName(),
                period
        );
        return payment.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(Map.of()));
    }
}
