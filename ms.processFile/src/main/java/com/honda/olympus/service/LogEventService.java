package com.honda.olympus.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.honda.olympus.vo.EventVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LogEventService {
	@Value("${logevent.service.url}")
	private String notificationURI;

	public void sendLogEvent(EventVO message) {
		try {
			log.info("Calling logEvent service: {}",message.toString());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			RestTemplate restTemplate = new RestTemplate();

			HttpEntity<EventVO> requestEntity = new HttpEntity<>(message, headers);

			ResponseEntity<String> responseEntity = restTemplate.postForEntity(notificationURI, requestEntity,
					String.class);

			log.info("LogEvent created with Status Code: {}",responseEntity.getStatusCode());
			log.info("Message: {}",responseEntity.getBody());
		} catch (Exception e) {
			log.info("Error calling logEvent service {}",e.getLocalizedMessage());
		}

	}
}
