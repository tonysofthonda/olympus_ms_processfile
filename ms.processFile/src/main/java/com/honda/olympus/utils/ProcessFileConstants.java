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
	
	public static final String FILE_ERROR_MESSAGE = "101 Error al guardar en la BD. La orden de producción %s no se guardo correctamente en la BD";
	public static final String DATA_ERROR_MESSAGE = "108 Error en datos de Tabla. La orden de producción %s no se puede guardar debido a los datos";
	public static final String INSERT_SUCCESS = "CREATE - Guardado en AFE. El archivo: \n %s \n Tokens: \n %s \n fue guardado con exito en la BD de AFE";
	
	public static final String GM_ORD_REQ_EXTERN_CONFIG_ID = "GM-ORD-REQ-EXTERN-CONFIG-ID";
	
	public static final String GM_ORD_REQ_REQST_ID = "GM-ORD-REQ-REQST-ID";
	public static final String GM_ORD_REQ_ACTION = "GM-ORD-REQ-ACTION";
	
	public static final String SUCCESS_CREATE_MESSAGE = "CREATE - Guardado en AFE. El archivo: \n %s \n Tokens: \n %s \n fue guardado con exito en la BD de AFE";
	
	public static final String GM_ORD_REQ_CHRG_BUSNS_ASCT_CD = "GM-ORD-REQ-CHRG-BUSNS-ASCT-CD";
	
	
	public static final String CLIENT_IP_TIMESTAMP = "Client IP: %s , TimeStamp: %s";
	
}
