package com.honda.olympus.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.persistence.EntityNotFoundException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.honda.olympus.controller.repository.AfeActionRepository;
import com.honda.olympus.controller.repository.AfeColorRepository;
import com.honda.olympus.controller.repository.AfeEventCodeRepository;
import com.honda.olympus.controller.repository.AfeFixedOrdersEvRepository;
import com.honda.olympus.controller.repository.AfeModelColorRepository;
import com.honda.olympus.controller.repository.AfeModelRepository;
import com.honda.olympus.controller.repository.AfeModelTypeRepository;
import com.honda.olympus.controller.repository.AfeOrdersHistoryRepository;
import com.honda.olympus.controller.repository.AfeEventStatusRepository;
import com.honda.olympus.dao.AfeActionEvEntity;
import com.honda.olympus.dao.AfeColorEntity;
import com.honda.olympus.dao.AfeFixedOrdersEvEntity;
import com.honda.olympus.dao.AfeModelColorEntity;
import com.honda.olympus.dao.AfeModelEntity;
import com.honda.olympus.dao.AfeModelTypeEntity;
import com.honda.olympus.dao.AfeOrdersActionHistoryEntity;
import com.honda.olympus.dao.AfeEventStatusEntity;
import com.honda.olympus.dao.EventCodeEntity;
import com.honda.olympus.exception.FileProcessException;
import com.honda.olympus.utils.FileprocessMessagesHandler;
import com.honda.olympus.utils.ProcessFileConstants;
import com.honda.olympus.utils.ProcessFileUtils;
import com.honda.olympus.vo.EventVO;
import com.honda.olympus.vo.MessageVO;
import com.honda.olympus.vo.TemplateFieldVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProcessFileService {

	@Autowired
	LogEventService logEventService;

	@Autowired
	FileprocessMessagesHandler fileprocessMessagesHandler;

	@Autowired
	NotificationService notificationService;

	@Value("${message.fail.status}")
	private String messageFailStatus;

	@Value("${message.fail.file.name}")
	private String messageFailName;

	@Value("${message.fail.file.exist}")
	private String messageFailExist;

	@Value("${message.fail.file.line}")
	private String messageFailLine;

	@Value("${max.line.size}")
	private Integer lineSize;

	@Value("${file.sourcedir}")
	private String HOME;

	@Value("${service.name}")
	private String serviceName;

	private static final String DELIMITER = "/";

	private static final String DATE_FORMAT = "yyyy-MM-dd";

	@Autowired
	AfeFixedOrdersEvRepository afeFixedOrdersEvRepository;

	@Autowired
	AfeColorRepository afeColorRepository;

	@Autowired
	AfeModelRepository afeModelRepository;

	@Autowired
	AfeModelColorRepository afeModelColorRepository;

	@Autowired
	AfeModelTypeRepository modelTypeRepository;

	@Autowired
	AfeActionRepository afeActionRepository;

	@Autowired
	AfeOrdersHistoryRepository afeOrdersHistoryRepository;

	@Autowired
	AfeEventStatusRepository afeEventStatusRepository;

	@Autowired
	AfeEventCodeRepository afeEventCodeRepository;

	private EventVO event = new EventVO();
	private JSONObject template;

	private String ipAddress;

	private MessageVO messageEventTableError;

	private String fileName;
	private Long modelIdQ5 = null;

	public void processFile(final MessageVO message, String ipAddress) throws FileProcessException, IOException {

		final Long status = message.getStatus();

		this.ipAddress = ipAddress;

		try {
			template = ProcessFileUtils.validateFileTemplate(lineSize);

		} catch (FileProcessException e) {
			event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, "Incorrect template specification",
					message.getFile());
			logEventService.sendLogEvent(event);
			throw e;
		}

		if (!ProcessFileConstants.ONE_STATUS.equals(status)) {

			message.setMsg(messageFailStatus);
			message.setStatus(ProcessFileConstants.ZERO_STATUS);
			logEventService.sendLogEvent(
					new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, messageFailStatus, message.getFile()));
			log.info(messageFailStatus);
			throw new FileProcessException(messageFailStatus);
		}

		if (message.getFile().isEmpty()) {

			message.setMsg(messageFailName);
			message.setStatus(ProcessFileConstants.ZERO_STATUS);
			logEventService.sendLogEvent(
					new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, messageFailName, message.getFile()));
			log.info(messageFailName);
			throw new FileProcessException(messageFailName);
		}

		messageEventTableError = new MessageVO(serviceName, ProcessFileConstants.ONE_STATUS,
				String.format(ProcessFileConstants.DATA_ERROR_MESSAGE, fileName), fileName);

		fileName = message.getFile();
		processFileData();

		log.info("ProcessFile:: File procesed");

	}

	private void processFileData() throws IOException, FileProcessException {

		List<List<TemplateFieldVO>> dataLines;
		Path path = Paths.get(HOME + DELIMITER + fileName);
		dataLines = new ArrayList<>();

		MessageVO messageEvent = new MessageVO(serviceName, ProcessFileConstants.ONE_STATUS,
				String.format(ProcessFileConstants.FILE_ERROR_MESSAGE, fileName), fileName);

		if (!Files.exists(path)) {

			log.info(messageFailExist + ": " + HOME + DELIMITER + fileName);
			logEventService.sendLogEvent(new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					messageFailExist + ": " + HOME + DELIMITER + fileName, fileName));
			// Notification message
			notificationService.generatesNotification(messageEvent);

			throw new FileProcessException(messageFailExist + ": " + HOME + DELIMITER + fileName);
		}

		try (Stream<String> input = Files.lines(path)) {
			input.forEach(line -> {

				if (line.length() == lineSize) {
					log.debug("Line: {}", line);
					log.debug("Line lenght: {}", line.length());
					dataLines.add(ProcessFileUtils.readProcessFileTemplate(template, line));
				} else {
					log.info("Processfile:: Invalid line will be discarted, due to size: {} ", line.length());
					fileprocessMessagesHandler.createAndLogMessageLineFail(line, fileName);
					// Notification message
					notificationService.generatesNotification(messageEvent);
				}
			});

		} catch (Exception e) {
			log.error("Exception opening file due to: {} ", e.getLocalizedMessage());
			fileprocessMessagesHandler.createAndLogMessageFileFail(fileName);
			fileprocessMessagesHandler.createAndLogMessageLineFail("Unknown", fileName);
			// Notification message
			notificationService.generatesNotification(messageEvent);
			
			throw new FileProcessException("No es posible abrir el archivo: " + fileName);

		}
		if (dataLines.isEmpty()) {

			notificationService.generatesNotification(messageEvent);
			throw new FileProcessException("No hay lineas para procesar en el archivo: " + fileName);
		}

		// Main reading lines loop
		for (List<TemplateFieldVO> dataList : dataLines) {

			if (dataList.get(0).lineNumber.length() != lineSize) {
				fileprocessMessagesHandler.createAndLogMessageLineFail(dataList.toString(), fileName);
				notificationService.generatesNotification(messageEvent);
			} else {

				// Obtain action to perform from line file
				// GM-ORD-REQ-ACTION
				Optional<TemplateFieldVO> actionFlow = ProcessFileUtils.getLineValueOfField(dataList,
						ProcessFileConstants.GM_ORD_REQ_ACTION);

				if (actionFlow.isPresent()) {
					log.debug("----------------- Operation: {} ---------------", actionFlow.get().getValue());
					if (actionFlow.get().getValue().equalsIgnoreCase(ProcessFileConstants.CREATE)) {
						createFlow(dataList);
					} else {

						if (actionFlow.get().getValue().equalsIgnoreCase(ProcessFileConstants.CHANGE)) {
							changeFlow(dataList);
						} else {

							if (actionFlow.get().getValue().equalsIgnoreCase(ProcessFileConstants.CANCEL)) {
								cancelFlow(dataList);
							} else {
								log.debug("ProcessFile:: Operation undefied");
							}
						}

					}

				} else {
					log.debug("ProcessFile:: Operation undefined");
					fileprocessMessagesHandler.createAndLogMessageActionNotDefinedFail(actionFlow.get().getValue(), fileName);
				}

			}
		}

	}

	private void createFlow(List<TemplateFieldVO> dataLine) throws FileProcessException {
		log.debug("ProcessFile:: Start:: Create Flow");

		// AFE_FIXED_ORDERS
		// linea.GM-ORD-REQ-REQST-ID
		// QUERY1
		String idFixedOrder;
		try {
			idFixedOrder = getStringValueOfFieldInLine(dataLine, ProcessFileConstants.GM_ORD_REQ_REQST_ID);
		} catch (NumberFormatException e) {
			return;
		}

		List<AfeFixedOrdersEvEntity> fixedOrders = afeFixedOrdersEvRepository.findByRequestId(idFixedOrder.trim());

		if (!fixedOrders.isEmpty()) {
			log.debug("ProcessFile:: FixedOrder exists");
			fileprocessMessagesHandler.createAndLogMessageReqstExist(fixedOrders.get(0).getRequestId(),
					"SELECT o FROM AfeFixedOrdersEvEntity o WHERE o.requestId = :requestId");

			// Sent email notification
			notificationService.generatesNotification(messageEventTableError);
			// return to main line process loop
			return;

		}

		// AFE_FIXED_ORDERS
		// linea.MDL_ID
		// QUERY2
		String externConfigId = getStringValueOfFieldInLine(dataLine, ProcessFileConstants.GM_ORD_REQ_EXTERN_CONFIG_ID);

		fixedOrders = afeFixedOrdersEvRepository.findByExternConfigId(externConfigId.trim());

		if (!fixedOrders.isEmpty()) {
			log.debug("ProcessFile:: ExternConfigId exists");
			fileprocessMessagesHandler.createAndLogMessageExternCondigId(externConfigId,
					"SELECT o FROM AfeFixedOrdersEvEntity o WHERE o.externConfigId = :externConfigId");

			// Sent email notification
			notificationService.generatesNotification(messageEventTableError);
			// return to main line process loop
			return;
		}

		// AFE_ACTION
		// modelColors.mdl_id
		// QUERY3
		String action = getStringValueOfFieldInLine(dataLine, ProcessFileConstants.GM_ORD_REQ_ACTION);

		List<AfeActionEvEntity> actions = afeActionRepository.findAllByAction(action.trim());
		if (actions.isEmpty()) {
			log.debug("ProcessFile:: Action no exists");
			fileprocessMessagesHandler.createAndLogMessageActionNoExist(action,
					"SELECT o FROM AfeActionEvEntity o WHERE o.action = :action");

			// Sent email notification
			notificationService.generatesNotification(messageEventTableError);

			// return to main line process loop
			return;
		}

		Long actionIdQ3 = actions.get(0).getId();

		
		
        if(!modelAndColorValidation(dataLine)) {
        	return;
        }

		// QUERY6
		AfeFixedOrdersEvEntity fixedOrder = new AfeFixedOrdersEvEntity();

		try {

			fixedOrder.setEnvioFlagGm(Boolean.FALSE);
			fixedOrder.setActionId(actionIdQ3);
			fixedOrder.setModelColorId(modelIdQ5);
			fixedOrder.setOrderNumber(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-VEH-ORD-NO"));
			fixedOrder.setSellingCode("");
			fixedOrder.setOriginType(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-ORIGIN-TYPE"));
			fixedOrder.setExternConfigId(
					getStringValueOfFieldInLine(dataLine, ProcessFileConstants.GM_ORD_REQ_EXTERN_CONFIG_ID));
			fixedOrder.setOrderType(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-ORD-TYP-CD").trim());
			fixedOrder.setChrgAsct(
					getLongValueOfFieldInLine(dataLine, ProcessFileConstants.GM_ORD_REQ_CHRG_BUSNS_ASCT_CD));
			fixedOrder.setChrgFcn(getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-CHRG-BUSNS-FCN-CD"));
			fixedOrder.setShipSct(getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-SHIP-BUSNS-ASCT-CD"));
			fixedOrder.setShipFcn(getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-SHIP-BUSNS-FCN-CD"));
			fixedOrder.setRequestId(getStringValueOfFieldInLine(dataLine, ProcessFileConstants.GM_ORD_REQ_REQST_ID));
			fixedOrder.setVinNumber("");
			fixedOrder.setProdWeekStartDay(getDateValueOfFieldInLine(dataLine, "GM-PROD-WEEK-START-DAY"));
			fixedOrder.setOrdDueDt(getDateValueOfFieldInLine(dataLine, "GM-ORD-DUE-DT"));
			fixedOrder.setCreateOrdTimestamp(new Date());
			fixedOrder.setCreationTimeStamp(new Date());

			fixedOrder.setObs(String.format(ProcessFileConstants.CLIENT_IP_TIMESTAMP, this.ipAddress,
					ProcessFileUtils.getTimeStamp()));
			fixedOrder.setBstate(1);

		} catch (NumberFormatException | ParseException nfe) {
			log.info("Exception inserting data due to: {} ", nfe.getLocalizedMessage());
			fileprocessMessagesHandler.createAndLogMessageInsertFixedorderFailed("INSERT * INTO AFE_FIXED_ORDER_EV",
					nfe.getLocalizedMessage());

			// Sent email notification
			notificationService.generatesNotification(messageEventTableError);
			// return to main line process loop
			return;
		}
		try {
			// QUERY6
			// QUERY7
			afeFixedOrdersEvRepository.saveAndFlush(fixedOrder);
		} catch (Exception e) {
			log.info("ProcessFile:: End sixth altern flow");
			fileprocessMessagesHandler.createAndLogMessageInsertFixedorderFailed("INSERT * INTO AFE_FIXED_ORDER_EV",
					e.getLocalizedMessage());

			// Sent email notification
			notificationService.generatesNotification(messageEventTableError);
			// return to main line process loop

			return;
		}

		// QUERY8
		if (!insertOrderHistory(actionIdQ3, fixedOrder.getId(), dataLine,fixedOrder.getEnvioFlagGm())) {
			return;
		}

		fileprocessMessagesHandler.createAndLogMessageInsertHistorySuccess(dataLine.get(0).lineNumber,dataLine.toString());

		MessageVO messageEvent = new MessageVO(serviceName, ProcessFileConstants.ONE_STATUS, String
				.format(ProcessFileConstants.SUCCESS_CREATE_MESSAGE, dataLine.get(0).lineNumber, dataLine.toString()),
				fileName);
		notificationService.generatesNotification(messageEvent);

		log.debug("ProcessFile:: End:: Create Flow");

	}

	private void changeFlow(List<TemplateFieldVO> dataLine) {

		log.debug("ProcessFile:: Start:: Change Flow");

		// linea.GM-ORD-REQ-REQST-ID
		String requestIdQ9 = getStringValueOfFieldInLine(dataLine, ProcessFileConstants.GM_ORD_REQ_REQST_ID);
		String externConfigIdQ9 = getStringValueOfFieldInLine(dataLine,
				ProcessFileConstants.GM_ORD_REQ_EXTERN_CONFIG_ID);

		// QUERY9
		List<AfeFixedOrdersEvEntity> fixedOrders = afeFixedOrdersEvRepository
				.findByRequestAndExternConfigId(externConfigIdQ9, requestIdQ9);

		if (fixedOrders.isEmpty()) {
			log.debug("ProcessFile:: FixedOrder DOESN'T exist");
			fileprocessMessagesHandler.createAndLogMessageNoExistFixedOrder(requestIdQ9,
					"SELECT o FROM AfeFixedOrdersEvEntity o WHERE  o.requestId = :requestId AND o.externConfigId = :externConfigId ");

			notificationService.generatesNotification(messageEventTableError);
			// return to main line process loop
			return;
		}

		Long fixedOrderIdQ9 = fixedOrders.get(0).getId();

		// QUERY10
		String ordReqAction;
		try {
			ordReqAction = getStringValueOfFieldInLine(dataLine, ProcessFileConstants.GM_ORD_REQ_ACTION);
		} catch (NumberFormatException e) {
			return;
		}

		List<AfeActionEvEntity> actions = afeActionRepository.findAllByAction(ordReqAction.trim());

		if (actions.isEmpty()) {
			log.debug("AFE_ACTION_EV DOESN'T exist");
			fileprocessMessagesHandler.createAndLogMessageActionNoExists(ordReqAction,
					"SELECT o FROM AfeActionEvEntity o WHERE o.action = :action ");

			notificationService.generatesNotification(messageEventTableError);
			// return to main line process loop
			return;
		}

		Long actionIdQ10 = actions.get(0).getId();

		// QUERY11
		List<AfeEventStatusEntity> eventStatus = afeEventStatusRepository.findAllByFixedOrder(fixedOrderIdQ9);

		if (eventStatus.isEmpty()) {
			log.debug("ProcessFile:: FixedOrder DOESN'T exist in EVENT_STATUS");
			fileprocessMessagesHandler.createAndLogMessageNoEventStatusExist(fixedOrderIdQ9,
					"SELECT o FROM AfeEventStatusEntity o WHERE o.fixedOrderId = :fixedOrderId ");

			notificationService.generatesNotification(messageEventTableError);
			// return to main line process loop
			return;
		}

		Long eventCodeIdQ11 = eventStatus.get(0).getEventCodeId();

		// QUERY12
		List<EventCodeEntity> eventCodes = afeEventCodeRepository.findAllByEventCode(eventCodeIdQ11);

		if (eventCodes.isEmpty()) {
			log.debug("ProcessFile:: FixedOrder DOESN'T exist in EVENT_CODE");
			fileprocessMessagesHandler.createAndLogMessageNoEventCodeExist(eventCodeIdQ11,
					"SELECT o FROM EventCodeEntity o WHERE o.id = :eventCodeId ");

			notificationService.generatesNotification(messageEventTableError);
			// return to main line process loop
			return;

		}

		Long eventCodeNumberQ12 = eventCodes.get(0).getEventCodeNumber();

		// Code number validation
		if (eventCodeNumberQ12 < ProcessFileConstants.MAX_CANCEL_EVENT_CODE_NUMBER) {
			log.debug("ProcessFile:: El event_code_number NO es mayor o igual a {} ,event_coe_number: {} ",
					ProcessFileConstants.MAX_CANCEL_EVENT_CODE_NUMBER, eventCodeNumberQ12);

			fileprocessMessagesHandler.createAndLogMessageNoEventCodeMajor(
					ProcessFileConstants.MAX_CANCEL_EVENT_CODE_NUMBER, eventCodeNumberQ12);

			notificationService.generatesNotification(messageEventTableError);
			// return to main line process loop
			return;
		}

	
		if(!modelAndColorValidation(dataLine)) {
        	return;
        }

		MessageVO messageEvent = new MessageVO(serviceName, ProcessFileConstants.ONE_STATUS,
				String.format(ProcessFileConstants.FILE_ERROR_MESSAGE, fileName), fileName);
		
		AfeFixedOrdersEvEntity fixedOrder= new AfeFixedOrdersEvEntity();
		try {
			// QUERY15
			fixedOrder = afeFixedOrdersEvRepository.findFixedOrderById(fixedOrderIdQ9);

			fixedOrder.setEnvioFlagGm(Boolean.FALSE);
			fixedOrder.setActionId(actionIdQ10);
			fixedOrder.setModelColorId(modelIdQ5);
			fixedOrder.setOrderNumber(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-VEH-ORD-NO"));
			fixedOrder.setSellingCode("");
			fixedOrder.setOriginType(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-ORIGIN-TYPE"));
			fixedOrder.setExternConfigId(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-EXTERN-CONFIG-ID"));
			fixedOrder.setOrderType(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-ORD-TYP-CD"));

			fixedOrder.setChrgAsct(getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-CHRG-BUSNS-ASCT-CD"));
			fixedOrder.setChrgFcn(getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-CHRG-BUSNS-ASCT-CD"));

			fixedOrder.setShipSct(getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-SHIP-BUSNS-ASCT-CD"));
			fixedOrder.setShipFcn(getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-SHIP-BUSNS-FCN-CD"));

			fixedOrder.setRequestId(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-REQST-ID"));
			fixedOrder.setVinNumber("");
			fixedOrder.setProdWeekStartDay(getDateValueOfFieldInLine(dataLine, "GM-PROD-WEEK-START-DAY"));
			fixedOrder.setOrdDueDt(getDateValueOfFieldInLine(dataLine, "GM-ORD-DUE-DT"));
			fixedOrder.setChangeOrdTimestamp(new Date());
			fixedOrder.setUpdateTimeStamp(new Date());
			fixedOrder.setObs(String.format(ProcessFileConstants.CLIENT_IP_TIMESTAMP, this.ipAddress,
					ProcessFileUtils.getTimeStamp()));
			fixedOrder.setBstate(1);

			afeFixedOrdersEvRepository.saveAndFlush(fixedOrder);
		} catch (EntityNotFoundException e) {

			log.info("ProcessFile:: Error updating AFE_FIXED_ORDERS_EV");
			event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					"Fallo en la ejecuación del query de actualización en la tabla AFE_FIXED_ORDERS_EV con el query ",
					fileName);
			logEventService.sendLogEvent(event);

			// Sent email notification
			notificationService.generatesNotification(messageEvent);

		} catch (NumberFormatException | ParseException e) {

			// Sent email notification
			notificationService.generatesNotification(messageEvent);
			return;
		}

		// QUERY16
		if (!insertOrderHistory(actionIdQ10, fixedOrderIdQ9, dataLine,Boolean.FALSE)) {
			return;
		}

		String successMessage = "CHANGE - Guardado en AFE. El archivo: \n" + dataLine.get(0).lineNumber
				+ "\n Tokens: \n" + dataLine.toString() + "\n fue guardado con exito en la BD de AFE";

		fileprocessMessagesHandler.successMessage(dataLine.toString());

		messageEvent = new MessageVO(serviceName, ProcessFileConstants.ONE_STATUS, successMessage, fileName);
		notificationService.generatesNotification(messageEvent);

		log.debug("ProcessFile:: End:: Change Flow");

	}

	private void cancelFlow(List<TemplateFieldVO> dataLine) {
		log.debug("ProcessFile:: Start:: Cancel Flow");

		String idFixedOrder = getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-REQST-ID");
		String externConfigId = getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-EXTERN-CONFIG-ID");

		// QUERY17
		List<AfeFixedOrdersEvEntity> fixedOrders = afeFixedOrdersEvRepository
				.findByRequestAndExternConfigId(externConfigId.trim(), idFixedOrder.trim());

		if (fixedOrders.isEmpty()) {

			fileprocessMessagesHandler.createAndLogMessageNoExistFixedOrder(idFixedOrder,
					"SELECT o FROM AfeFixedOrdersEvEntity o WHERE  o.requestId = :requestId AND o.externConfigId = :externConfigId ");

			// email send notification
			notificationService.generatesNotification(messageEventTableError);
			// return to main line process loop
			log.debug("ProcessFile:: FixedOrder DOESN'T exist");
			return;
		}

		Long fixedOrderIdQ17 = fixedOrders.get(0).getId();

		String ordReqAction = getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-ACTION");

		// QUERY18
		List<AfeActionEvEntity> actions = afeActionRepository.findAllByAction(ordReqAction.trim());

		if (actions.isEmpty()) {
			log.debug("AFE_ACTION_EV DOESN'T exist");
			fileprocessMessagesHandler.createAndLogMessageActionNoExists(ordReqAction,
					"SELECT o FROM AfeActionEvEntity o WHERE o.action = :action ");

			// email send notification
			notificationService.generatesNotification(messageEventTableError);
			// return to main line process loop
			return;

		}
		Long idActionQ18 = actions.get(0).getId();

		// QUERY23
		List<AfeEventStatusEntity> eventStatus = afeEventStatusRepository.findAllByFixedOrder(fixedOrderIdQ17);

		if (eventStatus.isEmpty()) {
			log.debug("ProcessFile:: FixedOrder DOESN'T exist in EVENT_STATUS");
			fileprocessMessagesHandler.createAndLogMessageNoEventStatusExist(fixedOrderIdQ17,
					"SELECT o FROM AfeEventStatusEntity o WHERE o.fixedOrderId = :fixedOrderId ");

			notificationService.generatesNotification(messageEventTableError);
			// return to main line process loop
			return;
		}

		Long eventCodeIdQ23 = eventStatus.get(0).getEventCodeId();

		// QUERY24
		List<EventCodeEntity> eventCodes = afeEventCodeRepository.findAllByEventCode(eventCodeIdQ23);

		if (eventCodes.isEmpty()) {
			log.debug("ProcessFile:: FixedOrder DOESN'T exist in EVENT_CODE");
			fileprocessMessagesHandler.createAndLogMessageNoEventCodeExist(eventCodeIdQ23,
					"SELECT o FROM EventCodeEntity o WHERE o.id = :eventCodeId ");

			notificationService.generatesNotification(messageEventTableError);
			// return to main line process loop
			return;

		}
		
		Long eventCodeNumberQ24 = eventCodes.get(0).getEventCodeNumber();

		// Code number validation
		if (eventCodeNumberQ24 < ProcessFileConstants.MAX_CANCEL_EVENT_CODE_NUMBER) {
			log.debug("ProcessFile:: El event_code_number es mayor o igual a {} ,event_coe_number: {} ",
					ProcessFileConstants.MAX_CANCEL_EVENT_CODE_NUMBER, eventCodeNumberQ24);

			fileprocessMessagesHandler.createAndLogMessageNoEventCodeMajor(
					ProcessFileConstants.MAX_CANCEL_EVENT_CODE_NUMBER, eventCodeNumberQ24);

			notificationService.generatesNotification(messageEventTableError);
			// return to main line process loop
			return;
		}

		Long fixedOrderIdQ19 = null;
		AfeFixedOrdersEvEntity fixedOrder = new AfeFixedOrdersEvEntity();
		try {
			// QUERY19
			fixedOrder = afeFixedOrdersEvRepository.findFixedOrderById(fixedOrderIdQ17);

			fixedOrderIdQ19 = fixedOrder.getId();

			fixedOrder.setEnvioFlagGm(Boolean.TRUE);
			fixedOrder.setActionId(idActionQ18);
			fixedOrder.setSellingCode("");
			fixedOrder.setCancelOrdTimestamp(new Date());
			fixedOrder.setUpdateTimeStamp(new Date());
			fixedOrder.setObs(
					String.format("Client IP: %s , TimeStamp: %s", this.ipAddress, ProcessFileUtils.getTimeStamp()));
			fixedOrder.setBstate(1);

			afeFixedOrdersEvRepository.saveAndFlush(fixedOrder);
		} catch (EntityNotFoundException e) {

			log.info("ProcessFile:: Error updating AFE_FIXED_ORDERS_EV");
			event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					"Fallo en la ejecuación del query de actualización en la tabla AFE_FIXED_ORDERS_EV con el query ",
					fileName);
			logEventService.sendLogEvent(event);

		} catch (NumberFormatException e) {
			return;
		}

		// QUERY20
		if (!insertOrderHistory(idActionQ18, fixedOrderIdQ19, dataLine,Boolean.TRUE)) {
			return;
		}

		String successMessage = "CANCEL - Guardado en AFE. El archivo: \n" + dataLine.get(0).lineNumber
				+ "\n Tokens: \n" + dataLine.toString() + "\n fue guardado con exito en la BD de AFE";
		
		fileprocessMessagesHandler.successMessage(dataLine.toString());

		MessageVO messageEvent = new MessageVO(serviceName, ProcessFileConstants.ONE_STATUS, successMessage, fileName);
		notificationService.generatesNotification(messageEvent);

	}

	private boolean insertOrderHistory(Long actionId, Long fixedOrderId, List<TemplateFieldVO> dataLine,Boolean flagGm) {

		AfeOrdersActionHistoryEntity orderHistory = new AfeOrdersActionHistoryEntity();
		orderHistory.setActionId(actionId);
		orderHistory.setFixedOrderId(fixedOrderId);
		orderHistory.setEnvioFlagGm(flagGm);
		orderHistory.setCreationTimeStamp(new Date());
		orderHistory.setObs(
				String.format("Client IP: %s , TimeStamp: %s", this.ipAddress, ProcessFileUtils.getTimeStamp()));
		orderHistory.setBstate(1);

		try {
			afeOrdersHistoryRepository.save(orderHistory);
			return Boolean.TRUE;
		} catch (Exception e) {

			fileprocessMessagesHandler.createAndLogMessageLineInsertFail("INSERT INTO AFE_ORDER_HISTORY");

			MessageVO messageEvent = new MessageVO(serviceName, ProcessFileConstants.ONE_STATUS,
					String.format(ProcessFileConstants.FILE_ERROR_MESSAGE, fileName), fileName);
			
			
			notificationService.generatesNotification(messageEvent);
			return Boolean.FALSE;
		}
	}
	
	
	private Boolean modelAndColorValidation(List<TemplateFieldVO> dataLine) {
		
		// AFE_MODEL
		// QUERY4
		String modelCode = getStringValueOfFieldInLine(dataLine, "MDL-ID");
		Long lineModelYear = getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-MDL-YR");

		List<AfeModelEntity> models = afeModelRepository.findAllByCode(modelCode, lineModelYear);
		if (models.isEmpty()) {

			log.debug("ProcessFile:: Model no exists");
			fileprocessMessagesHandler.createAndLogMessageModelNoExist(modelCode, lineModelYear,
					"SELECT o FROM AfeModelEntity o WHERE o.code = :code and o.modelYear = :modelYear ");

			// Sent email notification
			notificationService.generatesNotification(messageEventTableError);
			// return to main line process loop
			return false;
		}

		Long modelIdQ4 = models.get(0).getId();
		Long modelTypeIdQ4 = models.get(0).getModelTypeId();

		String modeltypeId = getStringValueOfFieldInLine(dataLine, "MDL-TYP-ID");
		// QUERY 21
		List<AfeModelTypeEntity> modelTypes = modelTypeRepository.findAllByIdAndModel(modeltypeId, modelTypeIdQ4);
		if (modelTypes.isEmpty()) {
			log.debug("ProcessFile:: ModelType no exists");
			fileprocessMessagesHandler.createAndLogMessageModelTypeNoExist(modeltypeId,
					"SELECT o FROM AfeModelTypeEntity o WHERE o.modelType = :modelType AND o.id = :id");

			// Sent email notification
			notificationService.generatesNotification(messageEventTableError);
			// return to main line process loop
			return false;
		}

		String fileModelColorId = getStringValueOfFieldInLine(dataLine, "MDL-MFG-COLOR-ID");

		// QUERY22
		List<AfeColorEntity> colors = afeColorRepository.findAllByColorCode(fileModelColorId);

		if (colors.isEmpty()) {

			fileprocessMessagesHandler.createAndLogMessageColorNoExists(fileModelColorId,
					"SELECT o FROM AfeColorEntity o WHERE o.code = :code ");

			// Sent email notification
			notificationService.generatesNotification(messageEventTableError);
			// return to main line process loop
			return false;

		}

		AfeColorEntity colorQ6 = colors.get(0);
		Long colorIdQ22 = colorQ6.getId();

		// AFE_MODEL_COLOR
		// QUERY5
		List<AfeModelColorEntity> modelColors = afeModelColorRepository.findAllByModelIdAndColorId(modelIdQ4,
				colorIdQ22);
		if (modelColors.isEmpty()) {
			log.debug("ProcessFile:: ModelColor no exists");
			fileprocessMessagesHandler.createAndLogMessageModelColorNoExist(fileModelColorId, modelCode,
					"SELECT o FROM AfeModelColorEntity o WHERE o.modelId = :modelId AND o.colorId = :colorId");

			// Sent email notification
			notificationService.generatesNotification(messageEventTableError);
			// return to main line process loop
			return false;
		}

		modelIdQ5 = modelColors.get(0).getId();
		
		return true;
	}

	private Long getLongValueOfFieldInLine(List<TemplateFieldVO> dataLine, String fieldName)
			throws NumberFormatException {
		Optional<TemplateFieldVO> templateField = ProcessFileUtils.getLineValueOfField(dataLine, fieldName);

		Long longValue = null;
		if (templateField.isPresent()) {
			try {
				longValue = Long.parseLong(templateField.get().getValue().trim());
				// log.info("allowed long maxvalue: " + Long.MAX_VALUE);
			} catch (NumberFormatException e) {

				log.debug(
						"ProcessFile::  La línea leida no cumple con los requerimientos establecidos: format Number exception: {}",
						templateField.get().getValue());
				logEventService.sendLogEvent(new EventVO("ms.profile", ProcessFileConstants.ZERO_STATUS,
						"La línea leida no cumple con los requerimientos establecidos: format Number exception: "
								+ templateField.get().getValue(),
						fileName));

				throw e;
			}

		}

		return longValue;

	}

	private String getStringValueOfFieldInLine(List<TemplateFieldVO> dataLine, String fieldName) {
		Optional<TemplateFieldVO> templateField = ProcessFileUtils.getLineValueOfField(dataLine, fieldName);

		String stringValue = "";
		if (templateField.isPresent()) {
			try {
				stringValue = templateField.get().getValue().trim();
			} catch (Exception e) {

				log.debug(
						"ProcessFile::  La línea leida no cumple con los requerimientos establecidos: format exception: {}",
						templateField.get().getValue());
				logEventService.sendLogEvent(new EventVO("ms.profile", ProcessFileConstants.ZERO_STATUS,
						"La línea leida no cumple con los requerimientos establecidos: format exception: "
								+ templateField.get().getValue(),
						fileName));

				return "";
			}

		}

		return stringValue;

	}

	private Date getDateValueOfFieldInLine(List<TemplateFieldVO> dataLine, String fieldName) throws ParseException {

		Optional<TemplateFieldVO> templateField = ProcessFileUtils.getLineValueOfField(dataLine, fieldName);

		String strDate = null;
		if (templateField.isPresent()) {
			try {
				strDate = templateField.get().getValue();

				return new SimpleDateFormat(DATE_FORMAT).parse(strDate);

			} catch (ParseException ep) {

				log.debug(
						"ProcessFile::  La línea leida no cumple con los requerimientos establecidos due to:{}  date format exception for: {}",
						ep.getLocalizedMessage(), templateField.get().getValue());
				logEventService.sendLogEvent(new EventVO("ms.profile", ProcessFileConstants.ZERO_STATUS,
						"La línea leida no cumple con los requerimientos establecidos: format exception: "
								+ templateField.get().getValue(),
						fileName));

				throw ep;
			}

		}

		return null;
	}

}
