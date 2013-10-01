package com.czelink.server.base.handlers;

import java.io.Serializable;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.czelink.intg.exceptions.ServiceInvocationException;
import com.czelink.server.base.beans.JsonBaseViewBean;
import com.czelink.server.base.constants.BaseConstants;

@ControllerAdvice
public class ExceptionHandleAdvise implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log LOGGER = LogFactory
			.getLog(ExceptionHandleAdvise.class);

	@ExceptionHandler(Throwable.class)
	public @ResponseBody
	JsonBaseViewBean handleException(final Throwable th) {
		final JsonBaseViewBean response = new JsonBaseViewBean();
		response.setStatus(false);
		response.setStatusCode(BaseConstants.FATAL_ERROR_CODE);
		if (th instanceof ServiceInvocationException) {
			final ServiceInvocationException exception = (ServiceInvocationException) th;
			final String randomUid = exception.getCorrelationID();
			response.setCorrelationId(randomUid);
			if (ExceptionHandleAdvise.LOGGER.isErrorEnabled()) {
				ExceptionHandleAdvise.LOGGER.error("[CorrelationID: "
						+ randomUid + "]", th);
			}
		} else {
			final String randomUid = UUID.randomUUID().toString();
			response.setCorrelationId(randomUid);
			if (ExceptionHandleAdvise.LOGGER.isErrorEnabled()) {
				ExceptionHandleAdvise.LOGGER.error("[CorrelationID: "
						+ randomUid + "]", th);
			}
		}
		return response;
	}
}
