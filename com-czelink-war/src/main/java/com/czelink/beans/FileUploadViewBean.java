package com.czelink.beans;

import com.czelink.server.base.beans.JsonBaseViewBean;

public class FileUploadViewBean extends JsonBaseViewBean {
	
	private static final long serialVersionUID = 1L;
	
	private String src;

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}
}
