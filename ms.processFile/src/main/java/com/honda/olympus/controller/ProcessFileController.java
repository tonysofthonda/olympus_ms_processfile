package com.honda.olympus.controller;

import java.io.IOException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.honda.olympus.exception.FileProcessException;
import com.honda.olympus.service.ProcessFileService;
import com.honda.olympus.vo.MessageVO;
import com.honda.olympus.vo.ResponseVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ProcessFileController {

	@Autowired
	ProcessFileService processFileService;
	
	@Value("${service.success.message}")
	private String successMessage;

	@Value("${service.name}")
	private String serviceName;

	@PostMapping(value = "/file", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseVO> processFile(@Valid @RequestBody MessageVO message)
			throws FileProcessException, IOException {

		log.info("Calling File processing: {}",message.toString());

		processFileService.processFile(message);

		return new ResponseEntity<ResponseVO>(new ResponseVO(serviceName,1L,successMessage, message.getFile()), HttpStatus.OK);

	}

}
