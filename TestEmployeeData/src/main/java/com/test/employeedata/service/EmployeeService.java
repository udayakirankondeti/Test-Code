package com.test.employeedata.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.test.employeedata.exception.InvalidEmployeeDataException;
import com.test.employeedata.model.Employee;
import com.test.employeedata.repository.EmployeeRepository;
import com.test.employeedata.response.TaxDeductionResponse;

@Service
public class EmployeeService {

	@Autowired
	private EmployeeRepository employeeRepository;

	private static final BigDecimal TAX_SLAB_1 = BigDecimal.valueOf(250000);
	private static final BigDecimal TAX_SLAB_2 = BigDecimal.valueOf(500000);
	private static final BigDecimal TAX_SLAB_3 = BigDecimal.valueOf(1000000);
	private static final BigDecimal TAX_RATE_1 = BigDecimal.valueOf(0.05);
	private static final BigDecimal TAX_RATE_2 = BigDecimal.valueOf(0.10);
	private static final BigDecimal TAX_RATE_3 = BigDecimal.valueOf(0.20);
	private static final BigDecimal CESS_RATE = BigDecimal.valueOf(0.02);

	public Employee storeEmployeeDetails(Employee employee) {
		if (employee.getSalary().compareTo(BigDecimal.ZERO) <= 0) {
			throw new InvalidEmployeeDataException("Salary must be greater than zero");
		}
		return employeeRepository.save(employee);
	}

	public TaxDeductionResponse calculateTaxDeduction(Long id) {
		Optional<Employee> employee = employeeRepository.findById(id);
		if (!employee.isPresent()) {
			throw new InvalidEmployeeDataException("Invalid Employee ID is passed");
		}
		BigDecimal yearlySalary = calculateYearlySalary(employee.get());
		BigDecimal taxAmount = calculateTaxAmount(yearlySalary);
		BigDecimal cessAmount = calculateCessAmount(yearlySalary);

		TaxDeductionResponse response = new TaxDeductionResponse();
		response.setEmployeeCode(employee.get().getEmployeeId());
		response.setFirstName(employee.get().getFirstName());
		response.setLastName(employee.get().getLastName());
		response.setYearlySalary(yearlySalary);
		response.setTaxAmount(taxAmount);
		response.setCessAmount(cessAmount);

		return response;
	}

	private BigDecimal calculateYearlySalary(Employee employee) {
		LocalDate doj = employee.getDoj();
		LocalDate now = LocalDate.now();

		int monthsWorked = 0;
		if (doj.isBefore(now)) {
			monthsWorked = (int) (doj.until(now).toTotalMonths() + 1);
		}

		BigDecimal monthlySalary = employee.getSalary();
		BigDecimal yearlySalary = monthlySalary.multiply(BigDecimal.valueOf(monthsWorked));
		return yearlySalary;
	}

	private BigDecimal calculateTaxAmount(BigDecimal yearlySalary) {
		BigDecimal taxAmount = BigDecimal.ZERO;

		if (yearlySalary.compareTo(TAX_SLAB_1) > 0) {
			BigDecimal taxableAmount = yearlySalary.subtract(TAX_SLAB_1);
			if (taxableAmount.compareTo(TAX_SLAB_2.subtract(TAX_SLAB_1)) <= 0) {
				taxAmount = taxableAmount.multiply(TAX_RATE_1);
			} else if (taxableAmount.compareTo(TAX_SLAB_3.subtract(TAX_SLAB_1)) <= 0) {
				taxAmount = TAX_SLAB_2.subtract(TAX_SLAB_1).multiply(TAX_RATE_1)
						.add(taxableAmount.subtract(TAX_SLAB_2).multiply(TAX_RATE_2));
			} else {
				taxAmount = TAX_SLAB_2.subtract(TAX_SLAB_1).multiply(TAX_RATE_1)
						.add(TAX_SLAB_3.subtract(TAX_SLAB_2).multiply(TAX_RATE_2))
						.add(taxableAmount.subtract(TAX_SLAB_3).multiply(TAX_RATE_3));
			}
		}

		return taxAmount.setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal calculateCessAmount(BigDecimal yearlySalary) {
		BigDecimal cessAmount = BigDecimal.ZERO;

		if (yearlySalary.compareTo(BigDecimal.valueOf(2500000)) > 0) {
			BigDecimal excessAmount = yearlySalary.subtract(BigDecimal.valueOf(2500000));
			cessAmount = excessAmount.multiply(CESS_RATE);
		}

		return cessAmount.setScale(2, RoundingMode.HALF_UP);
	}
}
