package com.adham.accountservice.payment;

import com.adham.accountservice.auth.model.UserAccount;
import com.adham.accountservice.auth.repository.UserAccountRepository;
import com.adham.accountservice.payment.dto.PaymentRequest;
import com.adham.accountservice.payment.dto.PaymentResponse;
import com.adham.accountservice.payment.exception.PayrollOperationException;
import com.adham.accountservice.payment.model.Payroll;
import com.adham.accountservice.payment.repository.PayrollRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final UserAccountRepository userRepository;

    public PayrollService(
            PayrollRepository payrollRepository,
            UserAccountRepository userRepository
    ) {
        this.payrollRepository = payrollRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void addAll(List<PaymentRequest> requests) {
        List<Payroll> payrolls = new ArrayList<>();
        Set<String> requestPeriods = new HashSet<>();

        for (PaymentRequest request : requests) {
            UserAccount employee = findEmployee(request.getEmployee());
            YearMonth period = parsePeriod(request.getPeriod());
            String requestKey = employee.getId() + ":" + period;

            if (!requestPeriods.add(requestKey)
                    || payrollRepository.existsByEmployeeAndMonthAndYear(
                    employee,
                    period.getMonthValue(),
                    period.getYear()
            )) {
                throw new PayrollOperationException("Employee-period must be unique!");
            }

            payrolls.add(new Payroll(
                    employee,
                    period.getMonthValue(),
                    period.getYear(),
                    request.getSalary()
            ));
        }

        try {
            payrollRepository.saveAllAndFlush(payrolls);
        } catch (DataIntegrityViolationException exception) {
            throw new PayrollOperationException("Employee-period must be unique!");
        }
    }

    @Transactional
    public void update(PaymentRequest request) {
        UserAccount employee = findEmployee(request.getEmployee());
        YearMonth period = parsePeriod(request.getPeriod());
        Payroll payroll = payrollRepository.findByEmployeeAndMonthAndYear(
                        employee,
                        period.getMonthValue(),
                        period.getYear()
                )
                .orElseThrow(() -> new PayrollOperationException("Payroll not found!"));

        payroll.setSalary(request.getSalary());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getAll(String email) {
        UserAccount employee = findEmployee(email);
        return payrollRepository.findByEmployeeOrderByYearDescMonthDesc(employee)
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<PaymentResponse> getOne(String email, String requestedPeriod) {
        UserAccount employee = findEmployee(email);
        YearMonth period = parsePeriod(requestedPeriod);
        return payrollRepository.findByEmployeeAndMonthAndYear(
                employee,
                period.getMonthValue(),
                period.getYear()
        ).map(PaymentResponse::from);
    }

    private UserAccount findEmployee(String email) {
        return userRepository.findByEmailIgnoreCase(email.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new PayrollOperationException("Employee not found!"));
    }

    private YearMonth parsePeriod(String period) {
        if (period == null || !period.matches("(?:0[1-9]|1[0-2])-\\d{4}")) {
            throw new PayrollOperationException("Wrong date!");
        }

        int month = Integer.parseInt(period.substring(0, 2));
        int year = Integer.parseInt(period.substring(3));
        return YearMonth.of(year, month);
    }
}
