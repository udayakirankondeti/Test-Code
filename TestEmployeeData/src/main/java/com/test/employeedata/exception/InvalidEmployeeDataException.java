package com.test.employeedata.exception;

public class InvalidEmployeeDataException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidEmployeeDataException(String message) {
        super(message);
    }
}
