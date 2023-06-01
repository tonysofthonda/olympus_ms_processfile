package com.honda.olympus.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.honda.olympus.vo.MessageVO;
import com.honda.olympus.vo.ResponseVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationService {

	@Value("${notification.service.url}")
	private String notificationURI;

	public void generatesNotification(MessageVO message) {

		try {

			log.info("Calling Notification service: {}",message.toString());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			RestTemplate restTemplate = new RestTemplate();

			HttpEntity<MessageVO> requestEntity = new HttpEntity<>(message, headers);

			ResponseEntity<ResponseVO> responseEntity = restTemplate.postForEntity(notificationURI, requestEntity,
					ResponseVO.class);

			log.info("Notification sent with Status Code: {}",responseEntity.getStatusCode());
			log.info("Message: {}",responseEntity.getBody().getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			log.info("Error calling Notification service");
		}

	}

}
