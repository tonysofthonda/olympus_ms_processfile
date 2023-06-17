package com.honda.olympus.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;

import com.honda.olympus.exception.FileProcessException;
import com.honda.olympus.vo.TemplateFieldVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProcessFileUtils {
	
	
	private Integer totalEspaces =0;

	public static Properties fetchProperties() {
		Properties properties = new Properties();
		try {
			File file = ResourceUtils.getFile("classpath:application.properties");
			InputStream in = new FileInputStream(file);
			properties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}

	public static JSONObject validateFileTemplate(Integer control) throws FileProcessException {

		try {
			
			ClassPathResource staticDataResource = new ClassPathResource("processFileTemplate.json");
			
			File file = staticDataResource.getFile();
			log.debug("Resource FileName: {}",staticDataResource.getFilename());
			
			//File file = ResourceUtils.getFile("classpath:processFileTemplate.json");
			InputStream inputStream = new FileInputStream(file);
			StringBuilder responseStrBuilder = new StringBuilder();

			try (BufferedReader bR = new BufferedReader(new InputStreamReader(inputStream))) {
				String line = "";

				while ((line = bR.readLine()) != null) {

					responseStrBuilder.append(line);
				}

			} finally {
				inputStream.close();
			}

			JSONObject result = new JSONObject(responseStrBuilder.toString());

			JSONArray templateFields = result.getJSONArray("template");

			if (templateFields.isEmpty()) {
				return null;
			} else {

				final List<Integer> totCaracters = new ArrayList<>();
				final List<Integer> totDiferences = new ArrayList<>();

				templateFields.forEach(item -> {

					JSONObject obj = (JSONObject) item;
					totCaracters.add(obj.getInt("Spaces"));
					totDiferences.add((obj.getInt("Position_end") - obj.getInt("Position_start"))+1);
	
				});
				
				int sumCaracters = totCaracters.stream().mapToInt(Integer::intValue).sum();
				int sumDiferences = totDiferences.stream().mapToInt(Integer::intValue).sum();
				
				if(sumCaracters != control || sumDiferences != control) {
					
					throw new FileProcessException("Incorrect template specification");
					
				}

			}

			return result;
		} catch (IOException e) {
			
			log.info("Error reading or processing: processFileTemplate.json file: {}",e.getMessage());
			throw new FileProcessException("Error reading or processing: processFileTemplate.json file");
			
			
		}

	}

	public static List<TemplateFieldVO> readProcessFileTemplate(JSONObject template, final String line) {

		List<TemplateFieldVO> fileValues = new ArrayList<>();

		JSONArray templateFields = template.getJSONArray("template");

		if (templateFields.isEmpty()) {
			return null;
		} else {

			templateFields.forEach(item -> {

				JSONObject obj = (JSONObject) item;

				int start = obj.getInt("Position_start");
				int end = obj.getInt("Position_end");

				String fieldName = obj.getString("field");
				String value = line.substring(start-1, end);

				fileValues.add(new TemplateFieldVO(fieldName, value, line));

			});

		}

		return fileValues;

	}
	
	public static Optional<TemplateFieldVO> getLineValueOfField(List<TemplateFieldVO> dataLine,String fieldName) {
		
		return dataLine.stream().filter(c -> c.getFieldName().equalsIgnoreCase(fieldName)).findFirst();
		

	}
	
	


	public Integer getTotalEspaces() {
		return totalEspaces;
	}

	public void setTotalEspaces(Integer totalEspaces) {
		this.totalEspaces = totalEspaces;
	}
	 
	
	public static String getTimeStamp() {
		
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		return sdf.format(timestamp);
	}
	

}
