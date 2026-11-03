package com.adham.accountservice.payment.dto;

import com.adham.accountservice.payment.model.Payroll;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class PaymentResponse {

    private final String name;
    private final String lastname;
    private final String period;
    private final String salary;

    public PaymentResponse(String name, String lastname, String period, String salary) {
        this.name = name;
        this.lastname = lastname;
        this.period = period;
        this.salary = salary;
    }

    public static PaymentResponse from(Payroll payroll) {
        String monthName = Month.of(payroll.getMonth())
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        long salary = payroll.getSalary();

        return new PaymentResponse(
                payroll.getEmployee().getName(),
                payroll.getEmployee().getLastname(),
                monthName + "-" + payroll.getYear(),
                salary / 100 + " dollar(s) " + salary % 100 + " cent(s)"
        );
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }

    public String getPeriod() {
        return period;
    }

    public String getSalary() {
        return salary;
    }
}
