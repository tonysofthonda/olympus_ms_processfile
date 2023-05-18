package com.honda.olympus.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class MessageVO {

	private String source;

	@NotNull(message = "status is mandatory")
	private Long status;

	@NotBlank(message = "msg is mandatory")
	private String msg;

	private String file;

	public MessageVO(@NotBlank(message = "source is mandatory") String source,
			@Size(min = 1, max = 3, message = "staus not allowed") @NotNull(message = "status is mandatory") Long status,
			@NotBlank(message = "msg is mandatory") String msg, String file) {
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
		return "MessageVO [source=" + source + ", status=" + status + ", msg=" + msg + ", file=" + file + "]";
	}

}
