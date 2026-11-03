package com.adham.accountservice.payment.model;

import com.adham.accountservice.auth.model.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "payrolls",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_payroll_employee_period",
                columnNames = {"employee_id", "period_month", "period_year"}
        )
)
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private UserAccount employee;

    @Column(name = "period_month", nullable = false)
    private int month;

    @Column(name = "period_year", nullable = false)
    private int year;

    @Column(nullable = false)
    private long salary;

    protected Payroll() {
    }

    public Payroll(UserAccount employee, int month, int year, long salary) {
        this.employee = employee;
        this.month = month;
        this.year = year;
        this.salary = salary;
    }

    public UserAccount getEmployee() {
        return employee;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public long getSalary() {
        return salary;
    }

    public void setSalary(long salary) {
        this.salary = salary;
    }
}
