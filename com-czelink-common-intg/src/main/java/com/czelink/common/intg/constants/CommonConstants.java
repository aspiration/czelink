package com.czelink.common.intg.constants;

import java.io.Serializable;

public final class CommonConstants implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";

	public static final String ROLE_USER = "ROLE_USER";

	public static final String ROLE_ADMIN = "ROLE_ADMIN";

	public static final String ROLE_LIST_IN_SESSION_KEY = CommonConstants.class
			+ "_role_list_in_session";

	/**
	 * private constructor.
	 */
	private CommonConstants() {
		// no-operation.
	}
}
