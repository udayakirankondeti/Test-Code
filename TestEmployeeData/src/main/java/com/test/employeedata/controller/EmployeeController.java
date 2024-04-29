package com.test.employeedata.controller;

import com.test.employeedata.exception.InvalidEmployeeDataException;
import com.test.employeedata.exception.TaxCalculationException;
import com.test.employeedata.model.Employee;
import com.test.employeedata.service.EmployeeService;
import com.test.employeedata.response.TaxDeductionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<?> storeEmployeeDetails(@RequestBody Employee employee) {
        try {
            Employee savedEmployee = employeeService.storeEmployeeDetails(employee);
            return ResponseEntity.ok(savedEmployee);
        } catch (InvalidEmployeeDataException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/tax-deduction")
    public ResponseEntity<?> getTaxDeduction(@RequestParam(name = "id", required = true) Long id) {
        try {
            TaxDeductionResponse taxDeductionResponse = employeeService.calculateTaxDeduction(id);
            return ResponseEntity.ok(taxDeductionResponse);
        } catch (TaxCalculationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
