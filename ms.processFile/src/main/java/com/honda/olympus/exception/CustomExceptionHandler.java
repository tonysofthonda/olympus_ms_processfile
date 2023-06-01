package com.honda.olympus.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.honda.olympus.vo.ResponseVO;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler{
	@Value("${service.name}")
	private String serviceName;
	
	@ExceptionHandler(Exception.class)
	public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request){
		
		List<String> details = new ArrayList<>();
		
		details.add(ex.getLocalizedMessage());
		ResponseVO error = new ResponseVO(serviceName,0L,ex.getLocalizedMessage(), "");
		
		System.out.println(ex.getLocalizedMessage());
		
		return new ResponseEntity<>(error,HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(FileProcessException.class)
	public final ResponseEntity<Object> handlefileProcessException(Exception ex, WebRequest request){
		
		List<String> details = new ArrayList<>();
		
		details.add(ex.getLocalizedMessage());
		ResponseVO error = new ResponseVO(serviceName,0L,ex.getLocalizedMessage(), "");
		
		return new ResponseEntity<>(error,HttpStatus.BAD_REQUEST);
	}
}
