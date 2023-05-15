package com.honda.olympus.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.honda.olympus.exception.FileProcessException;
import com.honda.olympus.utils.ProcessFileUtils;
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

	private final static String ZERO_STATUS = "0";
	private final static String ONE_STATUS = "1";
	private final static String DELIMITER = "/";

	
	private List<List<TemplateFieldVO>> dataLines;

	public void processFile(final MessageVO message) throws FileProcessException, IOException {

		final String status = message.getStatus();

		ProcessFileUtils.readProcessFileTemplate();

		if (ONE_STATUS.equalsIgnoreCase(status)) {
			//
			message.setMsg(messageFailStatus);
			message.setStatus(ZERO_STATUS);
			logEventService.generatesLogEvent(message);

			throw new FileProcessException(messageFailStatus);
		}

		if (messageFailName.isEmpty()) {
			//
			message.setMsg(messageFailName);
			message.setStatus(ZERO_STATUS);
			logEventService.generatesLogEvent(message);

			throw new FileProcessException(messageFailName);
		}

		extractFileData(message.getFile());
		
		System.out.println("File procesed");

		

	}

	private void extractFileData(final String fileName) throws IOException, FileProcessException {

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
			}else {
				dataLines.add(new ArrayList<TemplateFieldVO>());
			}
			
			
		});

		/*
		if (getFirstLine().isEmpty() || getFirstLine().length() != lineSize) {			
			throw new FileProcessException(messageFailLine + ": " + fileName);
		}
		*/
		

	}

}
