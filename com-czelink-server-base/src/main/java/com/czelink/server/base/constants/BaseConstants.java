package com.czelink.server.base.constants;

import java.io.Serializable;

public final class BaseConstants implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String EMAIL_PATTERN = "[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})";

	public static final String FATAL_ERROR_CODE = "015";

	private BaseConstants() {
		// no-op.
	}

}
