package com.adham.accountservice.payment;

import com.adham.accountservice.payment.dto.OperationStatusResponse;
import com.adham.accountservice.payment.dto.PaymentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/acct/payments")
@Tag(name = "Payroll administration", description = "Accountant payroll operations")
@SecurityRequirement(name = "basicAuth")
public class AccountingPaymentController {

    private final PayrollService payrollService;

    public AccountingPaymentController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @PostMapping
    @Operation(summary = "Upload payroll entries")
    public ResponseEntity<OperationStatusResponse> addPayments(
            @RequestBody List<@Valid PaymentRequest> payments
    ) {
        payrollService.addAll(payments);
        return ResponseEntity.ok(new OperationStatusResponse("Added successfully!"));
    }

    @PutMapping
    @Operation(summary = "Update a payroll entry")
    public ResponseEntity<OperationStatusResponse> updatePayment(
            @Valid @RequestBody PaymentRequest payment
    ) {
        payrollService.update(payment);
        return ResponseEntity.ok(new OperationStatusResponse("Updated successfully!"));
    }
}
