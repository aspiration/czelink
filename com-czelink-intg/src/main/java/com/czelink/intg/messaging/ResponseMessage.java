package com.czelink.intg.messaging;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResponseMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private Object[] operationParameters;

	private Object returnValue;

	private final List<Object> exceptionList = new ArrayList<Object>();

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

	public List<Object> getExceptionList() {
		return Collections.unmodifiableList(this.exceptionList);
	}

	public void addToExceptionList(final Object execption) {
		this.exceptionList.add(execption);
	}

	public boolean getStatus() {
		return (null != this.returnValue)
				&& ((null == this.exceptionList) || (this.exceptionList.size() == 0));
	}
}
