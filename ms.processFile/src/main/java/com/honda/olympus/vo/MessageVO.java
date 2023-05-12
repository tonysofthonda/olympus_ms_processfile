package com.honda.olympus.vo;

public class MessageVO {

	private String source;
	private String status;
	private String msg;
	private String file;

	public MessageVO(String source, String status, String msg, String file) {
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
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
		return "MessageVO [source=" + source + ", status=" + status + ", msg=" + msg + ", file=" + file + "]";
	}

}
