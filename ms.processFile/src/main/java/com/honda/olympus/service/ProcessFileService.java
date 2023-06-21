package com.honda.olympus.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

		processFileData(message.getFile());

		log.info("ProcessFile:: File procesed");

	}

	private void processFileData(final String fileName) throws IOException, FileProcessException {

		List<List<TemplateFieldVO>> dataLines;
		Path path = Paths.get(HOME + DELIMITER + fileName);
		dataLines = new ArrayList<>();

		if (!Files.exists(path)) {

			log.info(messageFailExist + ": " + HOME + DELIMITER + fileName);
			logEventService.sendLogEvent(new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					messageFailExist + ": " + HOME + DELIMITER + fileName, fileName));
			throw new FileProcessException(messageFailExist + ": " + HOME + DELIMITER + fileName);
		}

		try (Stream<String> input = Files.lines(path)) {
			input.forEach(line -> {
				log.debug("Line: {}", line);
				log.debug("Line lenght: {}", line.length());
				dataLines.add(ProcessFileUtils.readProcessFileTemplate(template, line));
				
			});

		} catch (Exception e) {
			fileprocessMessagesHandler.createAndLogMessageFileFail(fileName);
			throw new FileProcessException("No es posible abrir el archivo: " + fileName);
		}

		// Main reading lines loop
		for (List<TemplateFieldVO> dataList : dataLines) {

			
			if (dataList.get(0).lineNumber.length() != lineSize) {
				fileprocessMessagesHandler.createAndLogMessageLineFail(dataList.toString(), fileName);

			} else {

				// Obtain action to perform from line file
				// GM-ORD-REQ-ACTION
				Optional<TemplateFieldVO> actionFlow = ProcessFileUtils.getLineValueOfField(dataList,
						"GM-ORD-REQ-ACTION");

				if (actionFlow.isPresent()) {
					log.debug("----------------- Operation: {} ---------------", actionFlow.get().getValue());
					if (actionFlow.get().getValue().equalsIgnoreCase(ProcessFileConstants.CREATE)) {
						createFlow(dataList, fileName);
					} else {

						if (actionFlow.get().getValue().equalsIgnoreCase(ProcessFileConstants.CHANGE)) {
							changeFlow(dataList, fileName);
						} else {

							if (actionFlow.get().getValue().equalsIgnoreCase(ProcessFileConstants.CANCEL)) {
								cancelFlow(dataList, fileName);
							} else {
								log.debug("ProcessFile:: Operation undefied");
							}
						}

					}

				} else {
					log.debug("ProcessFile:: Operation undefined");
				}

			}
		}

	}

	private void createFlow(List<TemplateFieldVO> dataLine, String fileName) throws FileProcessException {
		log.debug("ProcessFile:: Start:: Create Flow");

		// linea.GM-ORD-REQ-REQST-ID
		// QUERY1
		String idFixedOrder;
		try {
			idFixedOrder = getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-REQST-ID", fileName);
		} catch (NumberFormatException e) {
			return;
		}

		List<AfeFixedOrdersEvEntity> fixedOrders = afeFixedOrdersEvRepository.findByRequestId(idFixedOrder);

		if (!fixedOrders.isEmpty()) {
			log.debug("ProcessFile:: FixedOrder exists");
			fileprocessMessagesHandler.createAndLogMessageReqstExist(fixedOrders.get(0).getRequestId(), "");
			// return to main line process loop
			return;

		}

		// AFE_MODEL_COLOR
		// linea.MDL_ID
		// QUERY2
		String externConfigId;
		externConfigId = getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-EXTERN-CONFIG-ID", fileName);

		fixedOrders = afeFixedOrdersEvRepository.findByExternConfigId(externConfigId);

		if (!fixedOrders.isEmpty()) {
			log.debug("ProcessFile:: ExternConfigId exists");
			fileprocessMessagesHandler.createAndLogMessageExternCondigId(externConfigId, "");
			// return to main line process loop
			return;
		}

		// AFE_MODEL
		// modelColors.mdl_id
		// QUERY3
		String action = getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-ACTION", fileName);

		List<AfeActionEvEntity> actions = afeActionRepository.findAllByAction(action);
		if (actions.isEmpty()) {
			log.debug("ProcessFile:: Action no exists");
			fileprocessMessagesHandler.createAndLogMessageActionNoExist(action, "");

			// return to main line process loop
			return;
		}
		Long actionIdQ3 = actions.get(0).getId();

		// AFE_MODEL
		// QUERY4
		String modelCode = getStringValueOfFieldInLine(dataLine, "MDL-ID", fileName);

		List<AfeModelEntity> models = afeModelRepository.findAllByCode(modelCode);
		if (models.isEmpty()) {
			log.debug("ProcessFile:: Model no exists");
			fileprocessMessagesHandler.createAndLogMessageModelNoExist(modelCode, "");
			// return to main line process loop
			return;
		}

		Long modelId = models.get(0).getId();
		Long modelTypeId = models.get(0).getModelTypeId();
		Long plantId = models.get(0).getPlantId();
		Long modelYear = models.get(0).getModelYear();
		Long divisionId = models.get(0).getDivisionId();

		// AFE_MODEL_COLOR
		// QUERY5
		List<AfeModelColorEntity> modelColors = afeModelColorRepository.findAllByModelId(modelId);
		if (modelColors.isEmpty()) {
			log.debug("ProcessFile:: ModelColor no exists");
			fileprocessMessagesHandler.createAndLogMessageModelColorNoExist(modelId, "");
			// return to main line process loop
			return;
		}
		Long modelIdQ5 = modelColors.get(0).getModelId();

		// QUERY6
		AfeFixedOrdersEvEntity fixedOrder = new AfeFixedOrdersEvEntity();
		
		try {
		fixedOrder.setEnvioFlagGm(Boolean.FALSE);
		fixedOrder.setActionId(actionIdQ3);
		fixedOrder.setModelColorId(modelIdQ5);
		fixedOrder.setOrderNumber(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-VEH-ORD-NO", fileName));
		fixedOrder.setSellingCode("");
		fixedOrder.setOriginType(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-ORIGIN-TYPE", fileName));
		fixedOrder.setExternConfigId(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-EXTERN-CONFIG-ID", fileName));
		fixedOrder.setOrderType(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-ORD-TYP-CD", fileName));
		fixedOrder.setChrgAsct(getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-CHRG-BUSNS-ASCT-CD", fileName));
		fixedOrder.setChrgFcn(getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-CHRG-BUSNS-FCN-CD", fileName));
		fixedOrder.setRequestId(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-REQST-ID", fileName));
		fixedOrder.setVinNumber("");
		fixedOrder.setProdWeekStartDay(getDateValueOfFieldInLine(dataLine, "GM-PROD-WEEK-START-DAY", fileName));
		fixedOrder.setOrdDueDt(getDateValueOfFieldInLine(dataLine, "GM-ORD-DUE-DT", fileName));

		fixedOrder.setObs(
				String.format("Client IP: %s , TimeStamp: %s", this.ipAddress, ProcessFileUtils.getTimeStamp()));

		fixedOrder.setBstate(1);
		}catch(NumberFormatException nfe){
			log.info("Exception inserting data due to: {} ",nfe.getLocalizedMessage());
			fileprocessMessagesHandler.createAndLogMessageInsertFixedorderFailed("INSERT * INTO AFE_FIXED_ORDER_EV");
			throw new FileProcessException("Data format error in processed file");
		}
		try {
			// QUERY6
			// QUERY7
			afeFixedOrdersEvRepository.save(fixedOrder);
		} catch (Exception e) {
			log.info("ProcessFile:: End sixth altern flow");
			fileprocessMessagesHandler.createAndLogMessageInsertFixedorderFailed("INSERT * INTO AFE_FIXED_ORDER_EV");
		}

		// QUERY8
		if (!insertOrderHistory(actionIdQ3, fixedOrder.getId(), dataLine, fileName)) {
			return;
		}

		String successMessage = "Inserción exitosa de la línea:\n" + dataLine.get(0).lineNumber + "\n Tokens: \n"
				+ dataLine.toString() + "\n en la tabla AFE_FIXED_ORDERS_EV y en la tabla AFE_ORDERS_HISTORY";

		fileprocessMessagesHandler.createAndLogMessageInsertHistorySuccess(dataLine.toString());

		MessageVO messageEvent = new MessageVO(serviceName, ProcessFileConstants.ONE_STATUS, successMessage, fileName);
		notificationService.generatesNotification(messageEvent);

		log.debug("ProcessFile:: End:: Create Flow");

	}

	private void changeFlow(List<TemplateFieldVO> dataLine, String fileName) {

		log.debug("ProcessFile:: Start:: Change Flow");

		// linea.GM-ORD-REQ-REQST-ID
		// QUERY9
		Long idFixedOrder;
		Long externConfigId;
		try {
			idFixedOrder = getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-REQST-ID", fileName);
			externConfigId = getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-EXTERN-CONFIG-ID", fileName);

		} catch (NumberFormatException e) {
			return;
		}

		List<AfeFixedOrdersEvEntity> fixedOrders = afeFixedOrdersEvRepository
				.findByRequestAndExternConfigId(externConfigId, idFixedOrder);
		if (fixedOrders.isEmpty()) {

			fileprocessMessagesHandler.createAndLogMessageNoExistFixedOrder(idFixedOrder,
					"SELECT * FROM FIXED_ORDERS_EV WHERE EXTERN_CONFIG_ID AND ID_FIXED_ORDER");

			MessageVO messageEvent = new MessageVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					String.format("NO Existe el Id: %s en la tabla afedb.AFE_FIXED_ORDERS_EV con el query: %s",
							idFixedOrder, "SELECT * FROM FIXED_ORDERS_EV WHERE EXTERN_CONFIG_ID AND ID_FIXED_ORDER"),
					fileName);
			notificationService.generatesNotification(messageEvent);

			// return to main line process loop
			log.debug("ProcessFile:: FixedOrder DOESN'T exist");

		}

		Long fixedOrderIdQ9 = fixedOrders.get(0).getId();

		// query.idFixedOrder
		// QUERY10
		String ordReqAction;
		try {
			ordReqAction = getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-ACTION", fileName);
		} catch (NumberFormatException e) {
			return;
		}

		List<AfeActionEvEntity> actions = afeActionRepository.findAllByAction(ordReqAction);

		if (actions.isEmpty()) {
			fileprocessMessagesHandler.createAndLogMessageActionNoExists(ordReqAction,
					"SELECT * FROM AFE_ACTION WHERE ACTION");

			// return to main line process loop
			log.debug("AFE_ACTION_EV DOESN'T exist");

			return;

		}

		Long actionIdQ10 = actions.get(0).getId();

		// query.idFixedOrder
		// QUERY11
		List<AfeEventStatusEntity> eventStatus = afeEventStatusRepository.findAllByFixedOrder(fixedOrderIdQ9);

		if (eventStatus.isEmpty()) {
			fileprocessMessagesHandler.createAndLogMessageNoEventStatusExist(fixedOrderIdQ9,
					"SELECT * FROM EVENT_STATUS WHERE FIXED_ORDER_ID");

			MessageVO messageEvent = new MessageVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					String.format("NO tiene status el FIXED_ORDER: %s con el query: %s", fixedOrderIdQ9,
							"SELECT * FROM EVENT_STATUS WHERE FIXED_ORDER_ID"),
					fileName);
			notificationService.generatesNotification(messageEvent);
			// return to main line process loop
			log.debug("ProcessFile:: FixedOrder DOESN'T exist in EVENT_STATUS");

			return;

		}

		Long eventCodeIdQ11 = eventStatus.get(0).getEventCodeId();

		// QUERY12
		List<EventCodeEntity> eventCodes = afeEventCodeRepository.findAllByEventCode(eventCodeIdQ11);

		if (eventCodes.isEmpty()) {
			fileprocessMessagesHandler.createAndLogMessageNoEventCodeExist(eventCodeIdQ11,
					"SELECT * FROM EVENT_CODE WHERE EVENT_CODE_ID");

			MessageVO messageEvent = new MessageVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					String.format("NO existe el event code: %s con el query: %s", fixedOrderIdQ9,
							"SELECT * FROM EVENT_CODE WHERE EVENT_CODE_ID"),
					fileName);
			notificationService.generatesNotification(messageEvent);
			// return to main line process loop
			log.debug("ProcessFile:: FixedOrder DOESN'T exist in EVENT_CODE");

			return;

		}

		Long eventCodeNumberQ12 = eventCodes.get(0).getEventCodeNumber();

		if (eventCodeNumberQ12 >= ProcessFileConstants.MAX_CANCEL_EVENT_CODE_NUMBER) {

			fileprocessMessagesHandler.createAndLogMessageNoEventCodeMajor(
					ProcessFileConstants.MAX_CANCEL_EVENT_CODE_NUMBER, eventCodeNumberQ12);

			// return to main line process loop
			log.debug("ProcessFile:: El event_code_number es mayor o igual a {} ,event_coe_number: {} ",
					ProcessFileConstants.MAX_CANCEL_EVENT_CODE_NUMBER, eventCodeNumberQ12);

			return;
		}

		// QUERY13
		String modelCode = getStringValueOfFieldInLine(dataLine, "MDL-ID", fileName);

		List<AfeModelEntity> models = afeModelRepository.findAllByCode(modelCode);
		if (models.isEmpty()) {
			log.debug("ProcessFile:: Model no exists");
			fileprocessMessagesHandler.createAndLogMessageModelNoExist(modelCode, "");
			// return to main line process loop
			return;
		}

		Long modelId = models.get(0).getId();
		Long modelTypeId = models.get(0).getModelTypeId();
		Long plantId = models.get(0).getPlantId();
		Long modelYear = models.get(0).getModelYear();
		Long divisionId = models.get(0).getDivisionId();

		// AFE_MODEL_COLOR
		// QUERY14
		List<AfeModelColorEntity> modelColors = afeModelColorRepository.findAllByModelId(modelId);
		if (modelColors.isEmpty()) {
			log.debug("ProcessFile:: ModelColor no exists");
			fileprocessMessagesHandler.createAndLogMessageModelColorNoExist(modelId, "");
			// return to main line process loop
			return;
		}
		Long modelIdQ14 = modelColors.get(0).getModelId();

		try {
			// QUERY15
			AfeFixedOrdersEvEntity fixedOrder = afeFixedOrdersEvRepository.getById(idFixedOrder);

			fixedOrder.setEnvioFlagGm(Boolean.FALSE);
			fixedOrder.setActionId(actionIdQ10);
			fixedOrder.setModelColorId(modelIdQ14);
			fixedOrder.setOrderNumber(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-VEH-ORD-NO", fileName));
			fixedOrder.setSellingCode("");
			fixedOrder.setOriginType(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-ORIGIN-TYPE", fileName));
			fixedOrder
					.setExternConfigId(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-EXTERN-CONFIG-ID", fileName));
			fixedOrder.setOrderType(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-ORD-TYP-CD", fileName));
			fixedOrder.setChrgAsct(getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-CHRG-BUSNS-ASCT-CD", fileName));
			fixedOrder.setChrgFcn(getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-CHRG-BUSNS-FCN-CD", fileName));
			fixedOrder.setRequestId(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-REQST-ID", fileName));
			fixedOrder.setVinNumber("");
			fixedOrder.setProdWeekStartDay(getDateValueOfFieldInLine(dataLine, "GM-PROD-WEEK-START-DAY", fileName));
			fixedOrder.setOrdDueDt(getDateValueOfFieldInLine(dataLine, "GM-ORD-DUE-DT", fileName));
			fixedOrder.setChangeOrdTimestamp(new Date());
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

		// QUERY16
		if (!insertOrderHistory(actionIdQ10, fixedOrderIdQ9, dataLine, fileName)) {
			return;
		}

		String successMessage = "Actualización exitosa de la línea:\n" + dataLine.get(0).lineNumber + "\n Tokens: \n"
				+ dataLine.toString() + "\n en la tabla AFE_FIXED_ORDERS_EV y en la tabla AFE_ORDERS_HISTORY";

		fileprocessMessagesHandler.successMessage(dataLine.toString());

		MessageVO messageEvent = new MessageVO(serviceName, ProcessFileConstants.ONE_STATUS, successMessage, fileName);
		notificationService.generatesNotification(messageEvent);

		log.debug("ProcessFile:: End:: Change Flow");

	}

	private void cancelFlow(List<TemplateFieldVO> dataLine, String fileName) {
		log.debug("ProcessFile:: Start:: Cancel Flow");

		// QUERY17
		Long idFixedOrder;
		Long externConfigId;
		try {
			idFixedOrder = getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-REQST-ID", fileName);
			externConfigId = getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-EXTERN-CONFIG-ID", fileName);

		} catch (NumberFormatException e) {
			return;
		}

		List<AfeFixedOrdersEvEntity> fixedOrders = afeFixedOrdersEvRepository
				.findByRequestAndExternConfigId(externConfigId, idFixedOrder);

		if (fixedOrders.isEmpty()) {

			fileprocessMessagesHandler.createAndLogMessageNoExistFixedOrder(idFixedOrder,
					"SELECT * FROM FIXED_ORDERS_EV WHERE EXTERN_CONFIG_ID AND ID_FIXED_ORDER");

			MessageVO messageEvent = new MessageVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					String.format("NO Existe el Id: %s en la tabla afedb.AFE_FIXED_ORDERS_EV con el query: %s",
							idFixedOrder, "SELECT * FROM FIXED_ORDERS_EV WHERE EXTERN_CONFIG_ID AND ID_FIXED_ORDER"),
					fileName);
			notificationService.generatesNotification(messageEvent);

			// return to main line process loop
			log.debug("ProcessFile:: FixedOrder DOESN'T exist");

		}

		// QUERY18
		String ordReqAction;
		try {
			ordReqAction = getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-ACTION", fileName);
		} catch (NumberFormatException e) {
			return;
		}

		List<AfeActionEvEntity> actions = afeActionRepository.findAllByAction(ordReqAction);

		if (actions.isEmpty()) {
			fileprocessMessagesHandler.createAndLogMessageActionNoExists(ordReqAction,
					"SELECT * FROM AFE_ACTION WHERE ACTION");

			// return to main line process loop
			log.debug("AFE_ACTION_EV DOESN'T exist");

			return;

		}
		
		Long idActionQ18 = actions.get(0).getId();
		
		
		try {
			// QUERY15
			AfeFixedOrdersEvEntity fixedOrder = afeFixedOrdersEvRepository.getById(idFixedOrder);

			fixedOrder.setEnvioFlagGm(Boolean.TRUE);
			fixedOrder.setActionId(idActionQ18);
			fixedOrder.setSellingCode("");
			fixedOrder.setCancelOrdTimestamp(new Date());
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

	}

	private boolean insertOrderHistory(Long actionId, Long fixedOrderId, List<TemplateFieldVO> dataLine,
			String fileName) {

		AfeOrdersActionHistoryEntity orderHistory = new AfeOrdersActionHistoryEntity();
		orderHistory.setActionId(actionId);
		orderHistory.setFixedOrderId(fixedOrderId);
		orderHistory.setEnvioFlagGm(Boolean.FALSE);
		orderHistory.setCreationTimeStamp(new Date());
		orderHistory.setObs(
				String.format("Client IP: %s , TimeStamp: %s", this.ipAddress, ProcessFileUtils.getTimeStamp()));
		orderHistory.setBstate(1);

		try {
			afeOrdersHistoryRepository.save(orderHistory);
			return Boolean.TRUE;
		} catch (Exception e) {

			fileprocessMessagesHandler.createAndLogMessageLineInsertFail("INSERT INTO AFE_ORDER_HISTORY");

			MessageVO messageEvent = new MessageVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					"Fallo de ejecución del query de inserción en la tabla AFE_ORDER_HISOTRY con el query: INSERT INTO AFE_ORDER_HISTORY",
					fileName);
			notificationService.generatesNotification(messageEvent);
			return Boolean.FALSE;
		}
	}

	private Long getLongValueOfFieldInLine(List<TemplateFieldVO> dataLine, String fieldName, String fileName)
			throws NumberFormatException {
		Optional<TemplateFieldVO> templateField = ProcessFileUtils.getLineValueOfField(dataLine, fieldName);

		Long longValue = null;
		if (templateField.isPresent()) {
			try {
				longValue = Long.parseLong(templateField.get().getValue());
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

	private String getStringValueOfFieldInLine(List<TemplateFieldVO> dataLine, String fieldName, String fileName) {
		Optional<TemplateFieldVO> templateField = ProcessFileUtils.getLineValueOfField(dataLine, fieldName);

		String longValue = null;
		if (templateField.isPresent()) {
			try {
				longValue = templateField.get().getValue();
			} catch (Exception e) {

				log.debug(
						"ProcessFile::  La línea leida no cumple con los requerimientos establecidos: format exception: {}",
						templateField.get().getValue());
				logEventService.sendLogEvent(new EventVO("ms.profile", ProcessFileConstants.ZERO_STATUS,
						"La línea leida no cumple con los requerimientos establecidos: format exception: "
								+ templateField.get().getValue(),
						fileName));

				return null;
			}

		}

		return longValue;

	}

	private Date getDateValueOfFieldInLine(List<TemplateFieldVO> dataLine, String fieldName, String fileName) {

		Optional<TemplateFieldVO> templateField = ProcessFileUtils.getLineValueOfField(dataLine, fieldName);

		String longValue = null;
		if (templateField.isPresent()) {
			try {
				longValue = templateField.get().getValue();

				return new SimpleDateFormat("dd/MM/yyyy").parse(longValue);
	
			} catch (Exception e) {

				log.debug(
						"ProcessFile::  La línea leida no cumple con los requerimientos establecidos: format exception: {}",
						templateField.get().getValue());
				logEventService.sendLogEvent(new EventVO("ms.profile", ProcessFileConstants.ZERO_STATUS,
						"La línea leida no cumple con los requerimientos establecidos: format exception: "
								+ templateField.get().getValue(),
						fileName));

				return null;
			}

		}

		return null;
	}

}
