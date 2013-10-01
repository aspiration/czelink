package com.czelink.intg.messaging;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.czelink.intg.exceptions.ServiceInvocationException;

public class ResponseMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private Object[] operationParameters;

	private Object returnValue;

	private ServiceInvocationException exception;

	public Object[] getOperationParameters() {
		return operationParameters;
	}

	public void setOperationParameters(Object[] operationParameters) {
		this.operationParameters = operationParameters;
	}

	public Object getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(Object returnValue) {
		this.returnValue = returnValue;
	}

	public ServiceInvocationException getException() {
		return exception;
	}

	public void setException(ServiceInvocationException exception) {
		this.exception = exception;
	}

	public boolean getStatus() {
		return (null != this.returnValue) && (null == this.exception);
	}
}
