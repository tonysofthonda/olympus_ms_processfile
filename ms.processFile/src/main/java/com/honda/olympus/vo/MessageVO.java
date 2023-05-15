package com.honda.olympus.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class MessageVO {

	@Size(min = 1,max=3,message = "staus not allowed")
	@NotNull(message = "status is mandatory")
	private String status;
	
	@NotBlank(message = "msg is mandatory")
	private String msg;
	
	@NotBlank(message = "file is mandatory")
	private String file;

	public MessageVO(String status, String msg, String file) {
		super();
		this.status = status;
		this.msg = msg;
		this.file = file;
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
		return "MessageVO [status=" + status + ", msg=" + msg + ", file=" + file + "]";
	}

}
