package com.czelink.server.base.beans;

import java.io.Serializable;
import java.util.List;

public class JsonBaseViewBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String uid;

	private Boolean status;
	
	private String statusCode;
	
	private List<Object> validateErrors;

	public Boolean getStatus() {
		return status;
	}

	public void setStatus(Boolean status) {
		this.status = status;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public List<Object> getValidateErrors() {
		return validateErrors;
	}

	public void setValidateErrors(List<Object> validateErrors) {
		this.validateErrors = validateErrors;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	
}
