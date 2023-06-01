package com.honda.olympus.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.honda.olympus.controller.repository.AfeActionRepository;
import com.honda.olympus.controller.repository.AfeColorRepository;
import com.honda.olympus.controller.repository.AfeEventCodeRepository;
import com.honda.olympus.controller.repository.AfeFixedOrdersEvRepository;
import com.honda.olympus.controller.repository.AfeModelRepository;
import com.honda.olympus.controller.repository.AfeModelTypeRepository;
import com.honda.olympus.controller.repository.AfeOrdersHistoryRepository;
import com.honda.olympus.controller.repository.AfeStatusEvRepository;
import com.honda.olympus.dao.AfeActionEntity;
import com.honda.olympus.dao.AfeFixedOrdersEvEntity;
import com.honda.olympus.dao.AfeModelColorEntity;
import com.honda.olympus.dao.AfeModelEntity;
import com.honda.olympus.dao.AfeModelTypeEntity;
import com.honda.olympus.dao.AfeOrdersHistoryEntity;
import com.honda.olympus.dao.AfeStatusEvEntity;
import com.honda.olympus.dao.EventCodeEntity;
import com.honda.olympus.exception.FileProcessException;
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
	AfeModelRepository modelRepository;

	@Autowired
	AfeModelTypeRepository modelTypeRepository;

	@Autowired
	AfeActionRepository afeActionRepository;

	@Autowired
	AfeOrdersHistoryRepository afeOrdersHistoryRepository;

	@Autowired
	AfeStatusEvRepository afeStatusEvRepository;

	@Autowired
	AfeEventCodeRepository afeEventCodeRepository;

	private EventVO event = new EventVO();
	private JSONObject template;

	public void processFile(final MessageVO message) throws FileProcessException, IOException {

		final Long status = message.getStatus();

		try {
			template = ProcessFileUtils.validateFileTemplate(lineSize);
			
		} catch (FileProcessException e) {
			event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, "Incorrect template specification",message.getFile());
			logEventService.sendLogEvent(event);
			throw e;
		}
		

		if (!ProcessFileConstants.ONE_STATUS.equals(status)) {

			message.setMsg(messageFailStatus);
			message.setStatus(ProcessFileConstants.ZERO_STATUS);
			logEventService.sendLogEvent(
					new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, messageFailStatus, message.getFile()));

			throw new FileProcessException(messageFailStatus);
		}

		if (message.getFile().isEmpty()) {

			message.setMsg(messageFailName);
			message.setStatus(ProcessFileConstants.ZERO_STATUS);
			logEventService.sendLogEvent(
					new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, messageFailName, message.getFile()));

			throw new FileProcessException(messageFailName);
		}

		processFileData(message.getFile());

		log.info("File procesed");

	}

	private void processFileData(final String fileName) throws IOException, FileProcessException {

		List<List<TemplateFieldVO>> dataLines;
		Path path = Paths.get(HOME + DELIMITER + fileName);
		dataLines = new ArrayList<>();

		if (!Files.exists(path)) {
			logEventService.sendLogEvent(new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					messageFailExist + ": " + HOME + DELIMITER + "/" + fileName, fileName));
			throw new FileProcessException(messageFailExist + ": " + HOME + DELIMITER + "/" + fileName);
		}

		try (Stream<String> input = Files.lines(path)) {
			input.forEach(line -> {
				log.info("Line: {}",line);
				log.info("Line lenght: {}",line.length());

				if (line.length() == lineSize) {
					dataLines.add(ProcessFileUtils.readProcessFileTemplate(template, line));
				} else {
					dataLines.add(new ArrayList<>());
				}

			});

		} catch (Exception e) {
			logEventService.sendLogEvent(new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					"No es posible abrir el archivo: " + ": " + HOME + DELIMITER + "/" + fileName, fileName));
			throw new FileProcessException("No es posible abrir el archivo: " + fileName);
		}

		// Main reading lines loop
		for (List<TemplateFieldVO> dataList : dataLines) {

			if (dataList.isEmpty()) {
				log.info(
						"La línea leida no cumple con los requerimientos establecidos: {}", dataList.toString());
				logEventService.sendLogEvent(new EventVO("ms.profile", ProcessFileConstants.ZERO_STATUS,
						"La línea leida no cumple con los requerimientos establecidos: ", fileName));

			} else {

				// Obtain action to perform from line file
				// GM-ORD-REQ-ACTION
				Optional<TemplateFieldVO> actionFlow = ProcessFileUtils.getLineValueOfField(dataList,
						"GM-ORD-REQ-ACTION");

				if (actionFlow.isPresent()) {
					log.info("----------------- Operation: " + actionFlow.get().getValue()
							+ " --------------------------");
					if (actionFlow.get().getValue().equalsIgnoreCase(ProcessFileConstants.CREATE)) {
						createFlow(dataList, fileName);
					} else {

						if (actionFlow.get().getValue().equalsIgnoreCase(ProcessFileConstants.CHANGE)
								|| actionFlow.get().getValue().equalsIgnoreCase(ProcessFileConstants.CANCEL)) {
							cancelChangeFlow(dataList, fileName);
						} else {
							log.info("Operation undefied");
						}

					}

				} else {
					log.info("Operation undefined");
				}

			}
		}

	}

	@Transactional
	private void createFlow(List<TemplateFieldVO> dataLine, String fileName) throws FileProcessException {
		log.info("Start:: Create Flow");

		// linea.GM-ORD-REQ-REQST-ID
		// QUERY1
		Long idFixedOrder;
		try {
			idFixedOrder = getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-REQST-ID", fileName);
		} catch (NumberFormatException e) {
			return;
		}

		List<AfeFixedOrdersEvEntity> fixedOrders = afeFixedOrdersEvRepository.findAllById(idFixedOrder);
		if (!fixedOrders.isEmpty()) {
			log.info("End first altern flow");
			event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					"Existe el Id: en la tabla afedb.afe_fixed_orders_ev con el query: ", fileName);
			logEventService.sendLogEvent(event);

			// return to main line process loop
			log.info("FixedOrder exist");
			return;

		}

		// AFE_MODEL_COLOR
		// linea.MDL_ID
		// QUERY2
		Long modelColorId;
		try {
			modelColorId = getLongValueOfFieldInLine(dataLine, "MDL-ID", fileName);
		} catch (NumberFormatException e) {
			return;
		}

		List<AfeModelColorEntity> modelColors = afeColorRepository.findAllById(modelColorId);
		if (modelColors.isEmpty()) {
			log.info("End second altern flow");
			event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					"Existe el MDL_ID: en la tabla afedb.afe_model_color con el query: ", fileName);
			logEventService.sendLogEvent(event);

			// return to main line process loop
			return;
		}

		// AFE_MODEL
		// modelColors.mdl_id
		// QUERY3
		Long modelId = modelColors.get(0).getModel_id();

		List<AfeModelEntity> models = modelRepository.findAllById(modelId);
		if (models.isEmpty()) {
			log.info("End third altern flow");
			event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					"Existe el MDL_ID: en la tabla afedb.afe_model con el query: ", fileName);
			logEventService.sendLogEvent(event);

			// return to main line process loop
			return;
		}
		Long modelYear = models.get(0).getModelYear();
		String code = models.get(0).getCode();
		Long modelTypeId = models.get(0).getModelTypeId();

		// AFE_MODEL
		// models.model_type_id
		// QUERY4

		List<AfeModelTypeEntity> modelTypes = modelTypeRepository.findAllById(modelTypeId);
		if (modelTypes.isEmpty()) {
			log.info("End fourth altern flow");
			event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					"Existe el MODEL_TYPE: en la tabla afedb.afe_model_type con el query: ", fileName);
			logEventService.sendLogEvent(event);

			// return to main line process loop
			return;
		}

		// AFE_MODEL
		// models.model_type_id
		// QUERY5
		String action = getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-ACTION", fileName);

		List<AfeActionEntity> actions = afeActionRepository.findAllByAction(action);
		if (actions.isEmpty()) {
			log.info("End fifth altern flow");
			event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					"Existe el MODEL_TYPE: en la tabla afedb.afe_model_type con el query: ", fileName);
			logEventService.sendLogEvent(event);

			// return to main line process loop
			return;
		}

		// QUERY6

		AfeFixedOrdersEvEntity fixedOrder = new AfeFixedOrdersEvEntity();
		fixedOrder.setEnvioFlag(Boolean.FALSE);
		fixedOrder.setActionId(actions.get(0).getId());
		fixedOrder.setOrderNumber(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-VEH-ORD-NO", fileName));
		fixedOrder.setModelColorId(getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-MDL-YR", fileName));
		fixedOrder.setSellingCode(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-SELLING-SRC-CD", fileName));
		fixedOrder.setOriginType(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-ORIGIN-TYPE", fileName));
		fixedOrder.setExternConfigId(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-EXTERN-CONFIG-CD", fileName));
		fixedOrder.setOrderType(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-ORD-TYP-CD", fileName));
		fixedOrder.setChrgAsct(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-CHRG-BUSNS-ASCT-CD", fileName));
		fixedOrder.setChrgFcm(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-CHRG-BUSNS-FCN-CD", fileName));
		fixedOrder.setShipSct(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-CHRG-BUSNS-ASCT-CD", fileName));
		fixedOrder.setShipFcm(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-CHRG-BUSNS-FCN-CD", fileName));
		fixedOrder.setRequestId(getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-REQST-ID", fileName));

		fixedOrder.setStartDay(getStringValueOfFieldInLine(dataLine, "GM-PROD-WEEK-START-DAY", fileName));
		fixedOrder.setDueDate(getStringValueOfFieldInLine(dataLine, "GM-ORD-DUE-DT", fileName));
		fixedOrder.setModelColorId(getLongValueOfFieldInLine(dataLine, "MDL-ID", fileName));
		fixedOrder.setCreationTimeStamp(new Date());

		try {
			afeFixedOrdersEvRepository.save(fixedOrder);
		} catch (Exception e) {
			log.info("End fifth altern flow");
			event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, "No se inserto correctamente la linea: "
					+ dataLine.get(0).lineNumber + " en la tabla afedb.afe_fixed_orders_ev", fileName);
			logEventService.sendLogEvent(event);
		}

		// QUERY7
		// QUERY8
		if (!insertOrderHistory(actions.get(0).getId(), fixedOrder.getId(), dataLine, fileName)) {
			return;
		}

		String successMessage = "Inserción exitosa de la línea:\n" + dataLine.get(0).lineNumber + "\n Tokens: \n"
				+ dataLine.toString() + "\n en la tabla AFE_FIXED_ORDERS_EV y en la tabla AFE_ORDERS_HISTORY";

		event = new EventVO(serviceName, ProcessFileConstants.ONE_STATUS, successMessage, fileName);
		logEventService.sendLogEvent(event);

		MessageVO messageEvent = new MessageVO(serviceName, ProcessFileConstants.ONE_STATUS, successMessage, fileName);
		notificationService.generatesNotification(messageEvent);

		log.info("End:: Create Flow");

	}

	private void cancelChangeFlow(List<TemplateFieldVO> dataLine, String fileName) {

		log.info("Start:: CHANGE/CANCEL Flow");

		// linea.GM-ORD-REQ-REQST-ID
		// QUERY8
		Long idFixedOrder;
		try {
			idFixedOrder = getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-REQST-ID", fileName);

		} catch (NumberFormatException e) {
			return;
		}

		List<AfeFixedOrdersEvEntity> fixedOrders = afeFixedOrdersEvRepository.findAllById(idFixedOrder);
		if (fixedOrders.isEmpty()) {
			log.info("End first cancel/change altern flow");
			event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					"NO Existe el Id: en la tabla afedb.afe_fixed_orders_ev con el query: ", fileName);
			logEventService.sendLogEvent(event);

			MessageVO messageEvent = new MessageVO(serviceName, ProcessFileConstants.ONE_STATUS,
					"NO Existe el Id: en la tabla afedb.afe_fixed_orders_ev con el query: ", fileName);
			notificationService.generatesNotification(messageEvent);

			// return to main line process loop
			log.info("FixedOrder DOESN'T exist");

		}

		// query.idFixedOrder
		// QUERY9

		List<AfeStatusEvEntity> statusEv = afeStatusEvRepository.findAllByFixedOrder(idFixedOrder);

		if (statusEv.isEmpty()) {
			log.info("End second cancel/change altern flow");
			event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					"NO Existe el status para el id: " + idFixedOrder + " ", fileName);
			logEventService.sendLogEvent(event);

			// return to main line process loop
			log.info("AFE_STATUS_EV DOESN'T exist");

			return;

		}

		// query.idFixedOrder
		// QUERY10
		List<EventCodeEntity> eventCode = afeEventCodeRepository.findAllByEventCode(statusEv.get(0).getEventCodeId());

		if (eventCode.isEmpty()) {
			log.info("End third cancel/change altern flow");
			event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					"NO Existe el event_code_number para el id: " + statusEv.get(0).getId() + " ", fileName);
			logEventService.sendLogEvent(event);

			// return to main line process loop
			log.info("FixedOrder DOESN'T exist");

			return;

		}

		if (eventCode.get(0).getEventCodeNumber() >= ProcessFileConstants.MAX_CANCEL_EVENT_CODE_NUMBER) {

			log.info("End fourth cancel/change altern flow,event_code_number < {} ",
					ProcessFileConstants.MAX_CANCEL_EVENT_CODE_NUMBER);
			event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					"El event_code_number es mayor o igual a 2500, " + " event_coe_number :"
							+ eventCode.get(0).getEventCodeNumber(),
					fileName);
			logEventService.sendLogEvent(event);

			// return to main line process loop
			log.info("El event_code_number es mayor o igual a 2500, " + " event_coe_number: {} ",
					eventCode.get(0).getEventCodeNumber());

			return;
		}

		// query.idFixedOrder
		// QUERY11

		try {
			AfeFixedOrdersEvEntity fixedOrder = afeFixedOrdersEvRepository.getById(idFixedOrder);

			fixedOrder.setEnvioFlag(Boolean.TRUE);
			fixedOrder.setOrderNumber(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-VEH-ORD-NO", fileName));
			fixedOrder.setModelColorId(getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-MDL-YR", fileName));
			fixedOrder.setSellingCode(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-SELLING-SRC-CD", fileName));
			fixedOrder.setOriginType(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-ORIGIN-TYPE", fileName));
			fixedOrder
					.setExternConfigId(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-EXTERN-CONFIG-CD", fileName));
			fixedOrder.setOrderType(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-ORD-TYP-CD", fileName));
			fixedOrder.setChrgAsct(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-CHRG-BUSNS-ASCT-CD", fileName));
			fixedOrder.setChrgFcm(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-CHRG-BUSNS-FCN-CD", fileName));
			fixedOrder.setShipSct(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-CHRG-BUSNS-ASCT-CD", fileName));
			fixedOrder.setShipFcm(getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-CHRG-BUSNS-FCN-CD", fileName));
			fixedOrder.setRequestId(getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-REQST-ID", fileName));

			fixedOrder.setStartDay(getStringValueOfFieldInLine(dataLine, "GM-PROD-WEEK-START-DAY", fileName));
			fixedOrder.setDueDate(getStringValueOfFieldInLine(dataLine, "GM-ORD-DUE-DT", fileName));
			fixedOrder.setModelColorId(getLongValueOfFieldInLine(dataLine, "MDL-ID", fileName));
			fixedOrder.setUpdateTimeStamp(new Date());

			afeFixedOrdersEvRepository.saveAndFlush(fixedOrder);

		} catch (EntityNotFoundException e) {

			event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					"Fallo en la ejecuación del query de actualización en la tabla AFE_FIXED_ORDERS_EV con el query ",
					fileName);
			logEventService.sendLogEvent(event);

			// return to main line process loop
			log.info("Error updating AFE_FIXED_ORDERS_EV");

		} catch (NumberFormatException e) {
			return;
		}

		// query.idFixedOrder
		// QUERY12
		String actionId = getStringValueOfFieldInLine(dataLine, "GM-ORD-REQ-ACTION", fileName);
		List<AfeActionEntity> action = afeActionRepository.findAllByAction(actionId);

		if (action.isEmpty()) {
			log.info("End third cancel/change altern flow");
			event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS,
					"NO Existe la acción " + actionId + " en la tabla AFE_ACTION con el query: ", fileName);
			logEventService.sendLogEvent(event);

			// return to main line process loop
			log.info("AFE_ACTION DOESN'T exist");

			return;

		}

		// query.idFixedOrder
		// QUERY13
		if (!insertOrderHistory(action.get(0).getId(), idFixedOrder, dataLine, fileName)) {
			return;
		}

		String successMessage = "Actualización exitosa de la línea:\n" + dataLine.get(0).lineNumber + "\n Tokens: \n"
				+ dataLine.toString() + "\n en la tabla AFE_FIXED_ORDERS_EV y en la tabla AFE_ORDERS_HISTORY";

		event = new EventVO(serviceName, ProcessFileConstants.ONE_STATUS, successMessage, fileName);
		logEventService.sendLogEvent(event);

		MessageVO messageEvent = new MessageVO(serviceName, ProcessFileConstants.ONE_STATUS, successMessage, fileName);
		notificationService.generatesNotification(messageEvent);

		log.info("End:: Cancel/Change Flow");

	}

	private boolean insertOrderHistory(Long actionId, Long fixedOrderId, List<TemplateFieldVO> dataLine,
			String fileName) {

		AfeOrdersHistoryEntity orderHistory = new AfeOrdersHistoryEntity();
		orderHistory.setActionId(actionId);
		orderHistory.setFixedOrderId(fixedOrderId);
		orderHistory.setCreationTimeStamp(new Date());
		try {
			afeOrdersHistoryRepository.save(orderHistory);
			return Boolean.TRUE;
		} catch (Exception e) {
			log.info("Error inserting afedb.afe_fixed_orders_ev");
			event = new EventVO(serviceName, ProcessFileConstants.ZERO_STATUS, "No se inserto correctamente la linea: "
					+ dataLine.get(0).lineNumber + " en la tabla afedb.afe_orders_history", fileName);
			logEventService.sendLogEvent(event);

			MessageVO messageEvent = new MessageVO(serviceName, ProcessFileConstants.ONE_STATUS,
					"No se inserto correctamente la linea: " + dataLine.get(0).lineNumber
							+ " en la tabla afedb.afe_orders_history",
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

				log.info(
						"La línea leida no cumple con los requerimientos establecidos: format Number exception: {}",
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

				log.info("La línea leida no cumple con los requerimientos establecidos: format exception: {}",
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

}
