package com.adham.accountservice.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public class PaymentRequest {

    @NotBlank
    private String employee;

    @NotBlank
    @Pattern(regexp = "(?:0[1-9]|1[0-2])-\\d{4}", message = "Wrong date!")
    private String period;

    @NotNull
    @PositiveOrZero(message = "Salary must be non negative!")
    private Long salary;

    public PaymentRequest() {
    }

    public String getEmployee() {
        return employee;
    }

    public void setEmployee(String employee) {
        this.employee = employee;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Long getSalary() {
        return salary;
    }

    public void setSalary(Long salary) {
        this.salary = salary;
    }
}
