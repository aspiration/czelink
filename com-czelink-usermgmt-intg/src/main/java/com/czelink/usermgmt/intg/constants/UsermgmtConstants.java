package com.czelink.usermgmt.intg.constants;

import java.io.Serializable;

public final class UsermgmtConstants implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String LOGON_TOKEN_KEY = UsermgmtConstants.class
			+ "_logon_token";

	public static final String EORROR_MSG_CDE = "ERROR_MESSAGE_CODE";

	public static final String ACTIVATE_URL_KEY = "ACTIVATE_URL_KEY";

	public static final String USER_NAME = "user_name";

	/**
	 * private constructor.
	 */
	private UsermgmtConstants() {
		// private constructor.
	}
}
