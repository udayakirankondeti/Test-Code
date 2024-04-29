package com.test.employeedata.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.test.employeedata.exception.InvalidEmployeeDataException;
import com.test.employeedata.model.Employee;
import com.test.employeedata.repository.EmployeeRepository;
import com.test.employeedata.response.TaxDeductionResponse;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private Validator validator;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    public void testStoreEmployeeDetails_ValidEmployee_Success() {
        Employee employee = new Employee(11L, "E001", "John", "Doe", "john@example.com", "1234567890", LocalDate.now(), BigDecimal.valueOf(5000));
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(employeeRepository.save(any())).thenReturn(employee);
        Employee savedEmployee = employeeService.storeEmployeeDetails(employee);
        assertEquals(employee, savedEmployee);
        verify(employeeRepository, times(1)).save(employee);
    }

    @Test
    public void testCalculateTaxDeduction_ValidEmployeeId_Success() {
        BigDecimal monthlySalary = BigDecimal.valueOf(41666.67); // Monthly salary = Yearly salary / 12
        Employee employee = new Employee(10L, "E001", "John", "Doe", "john@example.com", "1234567890", LocalDate.now(), monthlySalary);
        when(employeeRepository.findById(10L)).thenReturn(Optional.of(employee));

        TaxDeductionResponse taxDeductionResponse = employeeService.calculateTaxDeduction(10L);

        assertEquals("E001", taxDeductionResponse.getEmployeeCode());
        assertEquals("John", taxDeductionResponse.getFirstName());
        assertEquals("Doe", taxDeductionResponse.getLastName());
        assertEquals(monthlySalary.multiply(BigDecimal.valueOf(12)), taxDeductionResponse.getYearlySalary()); // Correcting the expected yearly salary calculation
        assertEquals(BigDecimal.valueOf(0), taxDeductionResponse.getTaxAmount());
        assertEquals(BigDecimal.valueOf(0), taxDeductionResponse.getCessAmount());
    }

    @Test
    public void testCalculateTaxDeduction_InvalidEmployeeId_ExceptionThrown() {
        when(employeeRepository.findById(12L)).thenReturn(Optional.empty());

        assertThrows(InvalidEmployeeDataException.class, () -> employeeService.calculateTaxDeduction(12L));
    }
}
