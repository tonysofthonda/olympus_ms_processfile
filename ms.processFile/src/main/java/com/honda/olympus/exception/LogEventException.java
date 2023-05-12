package com.honda.olympus.exception;

public class LogEventException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 84174288517848800L;

	public LogEventException(String message) {
		super(message);
	}

	public LogEventException(Throwable ex) {
		super(ex);
	}

	public LogEventException(String message, Throwable ex) {
		super(message, ex);
	}

}
