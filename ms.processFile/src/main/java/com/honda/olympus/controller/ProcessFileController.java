package com.honda.olympus.controller;

import java.io.IOException;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
public class ProcessFileController {
	
	@Autowired
	ProcessFileService processFileService;
	
	@PostMapping(value = "/olympus/v1/file", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ResponseVO> processFile(@Valid @RequestBody MessageVO message)
			throws FileProcessException, IOException {

		System.out.println(message.toString());
		
		processFileService.processFile(message);
			
		

		return new ResponseEntity<ResponseVO>(new ResponseVO("File processed successsfully", null), HttpStatus.OK);

	}
	
}
