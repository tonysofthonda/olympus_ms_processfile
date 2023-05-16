package com.honda.olympus.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.honda.olympus.controller.repository.AfeColorRepository;
import com.honda.olympus.controller.repository.AfeFixedOrdersEvRepository;
import com.honda.olympus.controller.repository.AfeModelRepository;
import com.honda.olympus.dao.AfeFixedOrdersEvEntity;
import com.honda.olympus.dao.AfeModel;
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

	@Autowired
	AfeFixedOrdersEvRepository AfeFixedOrdersEvRepository;

	@Autowired
	AfeColorRepository AfeColorRepository;
	
	@Autowired
	AfeModelRepository modelRepository;

	public EventVO event = new EventVO();

	public void processFile(final MessageVO message) throws FileProcessException, IOException {

		final String status = message.getStatus();

		ProcessFileUtils.readProcessFileTemplate();

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

		JSONObject template = ProcessFileUtils.readProcessFileTemplate();

		Files.lines(path).forEach(line -> {
			System.out.println(line);

			System.out.println(line.length());
			if (line.length() == lineSize) {
				dataLines.add(ProcessFileUtils.readProcessFileTemplate(template, line, 1));
			} else {
				dataLines.add(new ArrayList<TemplateFieldVO>());
			}

		});

		// Main reading lines loop
		for (List<TemplateFieldVO> dataList : dataLines) {

			if (dataList.isEmpty()) {
				System.out.println("La línea leida no cumple con los requerimientos establecidos: " + dataList);
				logEventService.sendLogEvent(new EventVO("ms.profile", ZERO_STATUS,
						"La línea leida no cumple con los requerimientos establecidos: ", fileName));

			} else {

				createFlow(fileName);

				// cancelChangeFlow();
			}
		}

	}

	private void createFlow(String fileName) {
		System.out.println("Start:: Create Flow");
		// GM-ORD-REQ-REQST-ID
		// QUERY1
		Collection<AfeFixedOrdersEvEntity> fixedOrders = AfeFixedOrdersEvRepository.findAllById("GM-ORD-REQ-REQST-ID");
		if (!fixedOrders.isEmpty()) {
			System.out.println("End first altern flow");
			event = new EventVO(serviceName, ZERO_STATUS,
					"Existe el Id: en la tabla afedb.afe_fixed_orders_ev con el query: ", fileName);
			logEventService.sendLogEvent(event);

			return;

		}

		// AFE_MODEL_COLOR
		// QUERY2
		Collection<AfeFixedOrdersEvEntity> modelColors = AfeColorRepository.findAllById(1L);
		if (!modelColors.isEmpty()) {
			System.out.println("End second altern flow");
			event = new EventVO(serviceName, ZERO_STATUS,
					"Existe el MDL_ID: en la tabla afedb.afe_model_color con el query: ", fileName);
			logEventService.sendLogEvent(event);

			return;
		}

		// AFE_MODEL
		// QUERY3
		Collection<AfeModel> models = modelRepository.findAllById(1L);
		if (!models.isEmpty()) {
			System.out.println("End second altern flow");
			event = new EventVO(serviceName, ZERO_STATUS,
					"Existe el MDL_ID: en la tabla afedb.afe_model con el query: ", fileName);
			logEventService.sendLogEvent(event);

			return;
		}

		System.out.println("End:: Create Flow");

	}

	private void cancelChangeFlow() {

		System.out.println("Cancel Flow");

	}

}
