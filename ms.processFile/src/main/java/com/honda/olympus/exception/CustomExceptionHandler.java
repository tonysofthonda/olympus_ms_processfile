package com.honda.olympus.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.honda.olympus.service.NotificationService;
import com.honda.olympus.utils.ProcessFileConstants;
import com.honda.olympus.vo.MessageVO;
import com.honda.olympus.vo.ResponseVO;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler{
	@Value("${service.name}")
	private String serviceName;
	
	@Value("${spring.datasource.url}")
	private String host;
	
	@Value("${spring.datasource.username}")
	private String user;
	
	@Autowired
	NotificationService notificationService;
	
	@ExceptionHandler(Exception.class)
	public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request){
		
		String notificationMessage = String.format(ProcessFileConstants.FILE_ERROR_MESSAGE, "Unknown");
		MessageVO message = new MessageVO(serviceName, ProcessFileConstants.ZERO_STATUS,notificationMessage , "");
		notificationService.generatesNotification(message);
		
		ResponseVO error = new ResponseVO(serviceName,0L,ex.getLocalizedMessage(), "");
		return new ResponseEntity<>(error,HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(FileProcessException.class)
	public final ResponseEntity<Object> handlefileProcessException(Exception ex, WebRequest request){
		
		List<String> details = new ArrayList<>();
		
		details.add(ex.getLocalizedMessage());
		ResponseVO error = new ResponseVO(serviceName,0L,ex.getLocalizedMessage(), "");
		
		return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(NumberFormatException.class)
	public final ResponseEntity<Object> handlefileNumberFormatException(Exception ex, WebRequest request){
		
		List<String> details = new ArrayList<>();
		
		details.add(ex.getLocalizedMessage());
		ResponseVO error = new ResponseVO(serviceName,0L,ex.getLocalizedMessage(), "");
		
		return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
	}
}
