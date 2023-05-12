package com.honda.olympus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.honda.olympus.service.NotificationService;

@RestController
public class ProcessFileController {
	
	@Autowired
	NotificationService notificationService;
	
	@GetMapping(value = "/olympus/v1/file")
	public void processFile() {
		try {
			notificationService.generatesNotification();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
