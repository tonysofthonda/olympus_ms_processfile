package com.honda.olympus.vo;

public class EventVO {
	private String source;
	private Long status;

	private String msg;

	private String file;

	public EventVO() {
		super();
	}

	public EventVO(String source, Long status, String msg, String file) {
		super();
		this.source = source;
		this.status = status;
		this.msg = msg;
		this.file = file;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Long getStatus() {
		return status;
	}

	public void setStatus(Long status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	@Override
	public String toString() {
		return "EventVO [source=" + source + ", status=" + status + ", msg=" + msg + ", file=" + file + "]";
	}
	
	

}
