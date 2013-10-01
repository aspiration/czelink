package com.czelink.intg.exceptions;

public class ServiceInvocationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String correlationID;

	public ServiceInvocationException(final String correlationID,
			final Throwable exception) {
		super("Serivce Invocation Fail.", exception);
		this.correlationID = correlationID;
	}

	public String getCorrelationID() {
		return correlationID;
	}
}
