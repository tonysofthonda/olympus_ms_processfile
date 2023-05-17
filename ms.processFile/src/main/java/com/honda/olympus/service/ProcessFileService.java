package com.honda.olympus.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.honda.olympus.controller.repository.AfeActionRepository;
import com.honda.olympus.controller.repository.AfeColorRepository;
import com.honda.olympus.controller.repository.AfeFixedOrdersEvRepository;
import com.honda.olympus.controller.repository.AfeModelRepository;
import com.honda.olympus.controller.repository.AfeModelTypeRepository;
import com.honda.olympus.dao.AfeActionEntity;
import com.honda.olympus.dao.AfeFixedOrdersEvEntity;
import com.honda.olympus.dao.AfeModelColorEntity;
import com.honda.olympus.dao.AfeModelEntity;
import com.honda.olympus.dao.AfeModelTypeEntity;
import com.honda.olympus.exception.FileProcessException;
import com.honda.olympus.utils.ProcessFileUtils;
import com.honda.olympus.vo.EventVO;
import com.honda.olympus.vo.MessageVO;
import com.honda.olympus.vo.TemplateFieldVO;

@Service
public class ProcessFileService {

	@Autowired
	LogEventService logEventService;

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

	private final static String ZERO_STATUS = "0";
	private final static String ONE_STATUS = "1";
	private final static String DELIMITER = "/";

	private static final String SUCCESS_MESSAGE = "Success";
	private static final String CREATE = "CREATE";
	private static final String CHANGE = "CHANGE";
	private static final String CANCEL = "CANCEL";

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

	private EventVO event = new EventVO();
	private JSONObject template;

	public void processFile(final MessageVO message) throws FileProcessException, IOException {

		final String status = message.getStatus();

		template = ProcessFileUtils.validateFileTemplate(lineSize);

		if (ONE_STATUS.equalsIgnoreCase(status)) {
			//
			message.setMsg(messageFailStatus);
			message.setStatus(ZERO_STATUS);
			logEventService.sendLogEvent(new EventVO(status, status, status, status));

			throw new FileProcessException(messageFailStatus);
		}

		if (messageFailName.isEmpty()) {
			//
			message.setMsg(messageFailName);
			message.setStatus(ZERO_STATUS);
			logEventService.sendLogEvent(new EventVO(status, status, status, status));

			throw new FileProcessException(messageFailName);
		}

		processFileData(message.getFile());

		System.out.println("File procesed");

	}

	private void processFileData(final String fileName) throws IOException, FileProcessException {

		List<List<TemplateFieldVO>> dataLines;
		Path path = Paths.get(HOME + DELIMITER + fileName);
		dataLines = new ArrayList<>();

		if (!Files.exists(path)) {
			throw new FileProcessException(messageFailExist + ": " + HOME + DELIMITER + "/" + fileName);
		}

		Files.lines(path).forEach(line -> {
			System.out.println(line);
			System.out.println(line.length());

			if (line.length() == lineSize) {
				dataLines.add(ProcessFileUtils.readProcessFileTemplate(template, line));
			} else {
				dataLines.add(new ArrayList<>());
			}

		});

		// Main reading lines loop
		for (List<TemplateFieldVO> dataList : dataLines) {

			if (dataList.isEmpty()) {
				System.out.println("La línea leida no cumple con los requerimientos establecidos: " + dataList);
				logEventService.sendLogEvent(new EventVO("ms.profile", ZERO_STATUS,
						"La línea leida no cumple con los requerimientos establecidos: ", fileName));

			} else {

				// Obtain action to perform from line file
				// GM-ORD-REQ-ACTION
				Optional<TemplateFieldVO> actionFlow = ProcessFileUtils.getLineValueOfField(dataList,
						"GM-ORD-REQ-ACTION");

				if (actionFlow.isPresent()) {
					System.out.println("operation: " + actionFlow.get().getValue());
					if (actionFlow.get().getValue().equalsIgnoreCase(CREATE)) {
						createFlow(dataList, fileName);
					} else {

						cancelChangeFlow();

					}

				} else {
					System.out.println("None defined operation");
				}

			}
		}

	}

	private void createFlow(List<TemplateFieldVO> dataLine, String fileName) throws FileProcessException {
		System.out.println("Start:: Create Flow");

		// linea.GM-ORD-REQ-REQST-ID
		// QUERY1
		Long idFixedOrder = getLongValueOfFieldInLine(dataLine, "GM-ORD-REQ-REQST-ID", fileName);

		List<AfeFixedOrdersEvEntity> fixedOrders = afeFixedOrdersEvRepository.findAllById(idFixedOrder);
		if (!fixedOrders.isEmpty()) {
			System.out.println("End first altern flow");
			event = new EventVO(serviceName, ZERO_STATUS,
					"Existe el Id: en la tabla afedb.afe_fixed_orders_ev con el query: ", fileName);
			logEventService.sendLogEvent(event);

			// return to main line process loop
			System.out.println("FixedOrder exist");
			return;

		}

		// AFE_MODEL_COLOR
		// linea.MDL_ID
		// QUERY2
		Long modelColorId = getLongValueOfFieldInLine(dataLine, "MDL-ID", fileName);

		List<AfeModelColorEntity> modelColors = afeColorRepository.findAllById(modelColorId);
		if (modelColors.isEmpty()) {
			System.out.println("End second altern flow");
			event = new EventVO(serviceName, ZERO_STATUS,
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
			System.out.println("End third altern flow");
			event = new EventVO(serviceName, ZERO_STATUS,
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
			System.out.println("End fourth altern flow");
			event = new EventVO(serviceName, ZERO_STATUS,
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
			System.out.println("End fifth altern flow");
			event = new EventVO(serviceName, ZERO_STATUS,
					"Existe el MODEL_TYPE: en la tabla afedb.afe_model_type con el query: ", fileName);
			logEventService.sendLogEvent(event);

			// return to main line process loop
			return;
		}

		AfeFixedOrdersEvEntity fixedOrder = new AfeFixedOrdersEvEntity();
		fixedOrder.setEnvioFlag(Boolean.FALSE);
		fixedOrder.setActionId(actions.get(0).getId());
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
		
		afeFixedOrdersEvRepository.save(fixedOrder);

		System.out.println("End:: Create Flow");

	}

	private void cancelChangeFlow() {

		System.out.println("Cancel Flow");

	}

	private Long getLongValueOfFieldInLine(List<TemplateFieldVO> dataLine, String fieldName, String fileName) {
		Optional<TemplateFieldVO> templateField = ProcessFileUtils.getLineValueOfField(dataLine, fieldName);

		Long longValue = null;
		if (templateField.isPresent()) {
			try {
				longValue = Long.parseLong(templateField.get().getValue());
				System.out.println("allowed long maxvalue: " + Long.MAX_VALUE);
			} catch (NumberFormatException e) {

				System.out.println(
						"La línea leida no cumple con los requerimientos establecidos: format Number exception: "
								+ templateField.get().getValue());
				logEventService.sendLogEvent(new EventVO("ms.profile", ZERO_STATUS,
						"La línea leida no cumple con los requerimientos establecidos: format Number exception: "
								+ templateField.get().getValue(),
						fileName));

				// return to main line process loop
				return null;
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

				System.out.println(
						"La línea leida no cumple con los requerimientos establecidos: format exception: "
								+ templateField.get().getValue());
				logEventService.sendLogEvent(new EventVO("ms.profile", ZERO_STATUS,
						"La línea leida no cumple con los requerimientos establecidos: format exception: "
								+ templateField.get().getValue(),
						fileName));

				// return to main line process loop
				return null;
			}

		}

		return longValue;

	}

}
