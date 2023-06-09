package com.honda.olympus.vo;

public class TemplateFieldVO {

	public String fieldName;
	public String value;
	public String lineNumber;

	public TemplateFieldVO(String fieldName, String value, String lineNumber) {
		super();
		this.fieldName = fieldName;
		this.value = value;
		this.lineNumber = lineNumber;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(String lineNumber) {
		this.lineNumber = lineNumber;
	}

	@Override
	public String toString() {
		return "[fieldName=" + fieldName + ", value=" + value + "]\n";
	}

}
