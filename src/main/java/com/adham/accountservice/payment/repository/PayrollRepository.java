package com.adham.accountservice.payment.repository;

import com.adham.accountservice.auth.model.UserAccount;
import com.adham.accountservice.payment.model.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    boolean existsByEmployeeAndMonthAndYear(UserAccount employee, int month, int year);

    Optional<Payroll> findByEmployeeAndMonthAndYear(UserAccount employee, int month, int year);

    List<Payroll> findByEmployeeOrderByYearDescMonthDesc(UserAccount employee);

    void deleteByEmployee(UserAccount employee);
}
