package com.honda.olympus.utils;

public class ProcessFileConstants {
	
	private ProcessFileConstants() {
	    throw new IllegalStateException("ProcessFileConstants class");
	  }

	
	public static final Long ZERO_STATUS = 0L;
	public static final Long ONE_STATUS = 1L;
	public static final String DELIMITER = "/";

	public static final String SUCCESS_MESSAGE = "Success";
	public static final String CREATE = "CREATE";
	public static final String CHANGE = "CHANGE";
	public static final String CANCEL = "CANCEL";

	public static final Long MAX_CANCEL_EVENT_CODE_NUMBER = 2500L;
}
