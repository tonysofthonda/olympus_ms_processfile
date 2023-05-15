package com.honda.olympus.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.ResourceUtils;

import com.honda.olympus.vo.TemplateFieldVO;

public class ProcessFileUtils {

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

	public static JSONObject readProcessFileTemplate() {

		try {
			File file = ResourceUtils.getFile("classpath:processFileTemplate.json");
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

				templateFields.forEach(item -> {

					JSONObject obj = (JSONObject) item;

					totCaracters.add(obj.getInt("Spaces"));

					int diference = obj.getInt("Position_end") - obj.getInt("Position_start");

					System.out.println(obj.toString());
					System.out.println("Dif: "+diference);
					System.out.println("Spaces: " + obj.getInt("Spaces"));

				});

			}

			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static List<TemplateFieldVO> readProcessFileTemplate(JSONObject template, final String line,
			final Integer lineNumber) {

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

				fileValues.add(new TemplateFieldVO(fieldName, value, lineNumber));

			});

		}

		return fileValues;

	}

}
