package com.honda.olympus.utils;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.honda.olympus.service.LogEventService;
import com.honda.olympus.vo.EventVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FileprocessMessagesHandler {

	@Autowired
	LogEventService logEventService;

	@Value("${service.name}")
	private String serviceName;
	
	@Value("${service.success.message}")
	private String successMessage;

	private static final String ERROR_OPENNING_FILE = "No es posible abrir el archivo: %s";
	private static final String LINE_FAIL = "La línea leida no cumple con los requerimientos establecidos: %s ";
	private static final String REQUST_ID_EXISTS = "Existe el requst_id: %s en la tabla afedb.afe_fixed_orders_ev con el query: %s";
	private static final String EXTERN_REQST_ID = 	"Existe el extern_config_id: %s en la tabla AFE_FIXED_ORDERS_EV con el query: %s ";
	private static final String ACTION_NO_EXISTS = "NO EXISTE la acción: %s en la tabla AFE_ACTION con el query: %s";
	private static final String MODEL_COLOR_NO_EXISTS = "No existe el model_id: %s en la tabla AFE_MODEL_COLOR con el query: %s";
	private static final String MODEL_NO_EXIST = "No existe el code: %s en la tabla AFE_MODEL con el query: %s";
	private static final String QUERY_EXECUTION_FAIL = "Fallo en la ejecución del query de inserción en la tabla AFE_FIXED_ORDERS_EV con el query: %s, Due to: %s";
	private static final String QUERY_INSERT_HISTORY_SUCCESS = "Inserción exitosa de la línea: %s en la tabla AFE_ORDER_HISTORY ";
	private static final String QUERY_NO_EXIST_AFE_FIXED_ORDER = "NO existe el id: %s en la tabla AFE_FIXED_ORDER_EV con el query: %s";
	private static final String QUERY_ACTION_NO_EXISTS = "NO EXISTE la acción: %s en la tabla AFE_ACTION  con el query: %s";
	private static final String QUERY_EVENT_STATUS_NO_EXISTS = "NO tiene status el FIXED_ORDER: %s con el query: %s";
	private static final String QUERY_EVENT_CODE_NO_EXISTS = "NO existe el event code: %s con el query: %s";
	private static final String QUERY_EVENT_CODE_NUM_MAJOR = "El event_code_number es mayor o igual a %s event_code_number: %s";
	private static final String ACTION_SUCCESS = "Inserción exitosa de la línea %s en la tabla AFEfIXED_ORDER_EV y en la tabla AFE_ORDER_HISTORY";
	private static final String ORDER_HISTORY_FAIL = "Fallo de ejecución en la tabla AFE_ORDER_HISOTRY con el query: %s";
	
	
	

	private String message = null;
	EventVO event = null;

	public void createAndLogMessageFileFail(String fileName) {

		this.message = String.format(ERROR_OPENNING_FILE, fileName);
		this.event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, message, fileName);

		sendAndLog();
	}
	
	public void createAndLogMessageLineFail(String line,String fileName) {

		this.message = String.format(LINE_FAIL, line, fileName);
		this.event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, message, fileName);

		sendAndLog();
	}

	public void createAndLogMessageReqstExist(String requestId,String query) {

		this.message = String.format(REQUST_ID_EXISTS, requestId, query);
		this.event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, message, "");

		sendAndLog();
	}
	
	
	public void createAndLogMessageExternCondigId(String externConfigId,String query) {

		this.message = String.format(EXTERN_REQST_ID,  externConfigId,query);
		this.event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, message, "");

		sendAndLog();
	}
	
	
	public void createAndLogMessageActionNoExist(String action,String query) {

		this.message = String.format(ACTION_NO_EXISTS, action, query);
		this.event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, message, "");

		sendAndLog();
	}
	
	public void createAndLogMessageModelNoExist(String action,String query) {

		this.message = String.format(MODEL_NO_EXIST, action, query);
		this.event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, message, "");

		sendAndLog();
	}
	
	public void createAndLogMessageModelColorNoExist(Long modelId,String query) {

		this.message = String.format(MODEL_COLOR_NO_EXISTS, modelId, query);
		this.event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, message, "");

		sendAndLog();
	}
	
	public void createAndLogMessageInsertFixedorderFailed(String query,String cause) {

		this.message = String.format(QUERY_EXECUTION_FAIL, query,cause);
		this.event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, message, "");

		sendAndLog();
	}
	
	public void createAndLogMessageInsertHistorySuccess(String line) {

		this.message = String.format(QUERY_INSERT_HISTORY_SUCCESS, line);
		this.event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, message, "");

		sendAndLog();
	}
	
	
	public void createAndLogMessageNoExistFixedOrder(String fixedOrderId,String query) {

		this.message = String.format(QUERY_NO_EXIST_AFE_FIXED_ORDER, fixedOrderId,query);
		this.event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, message, "");

		sendAndLog();
	}
	
	public void createAndLogMessageActionNoExists(String action,String query) {

		this.message = String.format(QUERY_ACTION_NO_EXISTS,action,query);
		this.event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, message, "");

		sendAndLog();
	}
	
	public void createAndLogMessageNoEventStatusExist(Long fixedOrderId, String query) {

		this.message = String.format(QUERY_EVENT_STATUS_NO_EXISTS,fixedOrderId,query);
		this.event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, message, "");

		sendAndLog();
	}
	
	public void createAndLogMessageNoEventCodeExist(Long fixedOrderId, String query) {

		this.message = String.format(QUERY_EVENT_CODE_NO_EXISTS,fixedOrderId,query);
		this.event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, message, "");

		sendAndLog();
	}
	
	public void createAndLogMessageNoEventCodeMajor(Long eventCodeNumonst, Long eventCodeNum) {

		this.message = String.format(QUERY_EVENT_CODE_NUM_MAJOR,eventCodeNumonst,eventCodeNum);
		this.event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, message, "");

		sendAndLog();
	}

	public void createAndLogMessageLineInsertFail(String query) {

		this.message = String.format(ORDER_HISTORY_FAIL,query);
		this.event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, message, "");

		sendAndLog();
	}
	
	

	public void successMessage(String line) {
	
		this.message = String.format(ACTION_SUCCESS,line);
		this.event = new EventVO(serviceName, ProcessFileConstants.ONE_STATUS, message, "");

		sendAndLog();
	}
	

	

	private void sendAndLog() {
		logEventService.sendLogEvent(this.event);
		log.debug(this.message);
	}

}
