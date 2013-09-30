package com.czelink.intg.messaging;

import java.io.Serializable;

public class RequestMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private String serviceName;

	private String serviceInterface;

	private String operationName;

	private Object[] operationParameters;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceInterface() {
		return serviceInterface;
	}

	public void setServiceInterface(String serviceInterface) {
		this.serviceInterface = serviceInterface;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public Object[] getOperationParameters() {
		return operationParameters;
	}

	public void setOperationParameters(Object[] operationParameters) {
		this.operationParameters = operationParameters;
	}
}
