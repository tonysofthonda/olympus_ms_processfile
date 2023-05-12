package com.honda.olympus.exception;

public class NotificationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5069719670138956542L;

	public NotificationException(String message) {
		super(message);
	}

	public NotificationException(Throwable ex) {
		super(ex);
	}

	public NotificationException(String message, Throwable ex) {
		super(message, ex);
	}

}
