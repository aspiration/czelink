package com.czelink.utils;

import java.io.Serializable;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.MessageSource;

public final class ComponentAvailabilityHook implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * message source.
	 */
	private transient MessageSource hookMessageSource;

	/**
	 * 
	 * @param componantName
	 * @return ComponentAvailabilityResult.
	 */
	public ComponentAvailabilityResult checkIfComponentAvailable(
			final String componantName) {
		ComponentAvailabilityResult result = ComponentAvailabilityResult.UNDEFINED;
		final String detectClass = this.hookMessageSource.getMessage(
				componantName, null, StringUtils.EMPTY, Locale.getDefault());
		if (StringUtils.isBlank(detectClass)) {
			result = ComponentAvailabilityResult.UNDEFINED;
		} else {
			try {
				Class.forName(detectClass);
				result = ComponentAvailabilityResult.AVAILABLE;
			} catch (ClassNotFoundException e) {
				result = ComponentAvailabilityResult.UNAVAILABLE;
			}
		}
		return result;
	}

	public MessageSource getHookMessageSource() {
		return hookMessageSource;
	}

	public void setHookMessageSource(MessageSource hookMessageSource) {
		this.hookMessageSource = hookMessageSource;
	}

	public static enum ComponentAvailabilityResult {
		UNDEFINED, UNAVAILABLE, AVAILABLE
	}
}
