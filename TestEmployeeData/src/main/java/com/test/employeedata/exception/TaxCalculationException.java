package com.test.employeedata.exception;

public class TaxCalculationException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TaxCalculationException(String message) {
        super(message);
    }
}
