package com.honda.olympus.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.honda.olympus.vo.EventVO;
import com.honda.olympus.vo.ResponseVO;

@Service
public class LogEventService {
	@Value("${logevent.service.url}")
	private String notificationURI;

	public void sendLogEvent(EventVO message) {
		try {
			System.out.println("Calling logEvent service");
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			RestTemplate restTemplate = new RestTemplate();

			HttpEntity<EventVO> requestEntity = new HttpEntity<>(message, headers);

			ResponseEntity<ResponseVO> responseEntity = restTemplate.postForEntity(notificationURI, requestEntity,
					ResponseVO.class);

			System.out.println("LogEvent created with Status Code: " + responseEntity.getStatusCode());
			System.out.println("Message: " + responseEntity.getBody().getMessage());
			System.out.println("Location: " + responseEntity.getHeaders().getLocation());
		} catch (Exception e) {
			System.out.println("Error calling logEvent service "+e.getLocalizedMessage());
		}

	}
}
