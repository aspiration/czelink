package com.czelink.usermgmt.beans;

import java.io.Serializable;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;

import com.czelink.server.base.constants.BaseConstants;
import com.czelink.usermgmt.constants.UsermgmtServerConstants;

public class RegisterFormBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotEmpty(message = UsermgmtServerConstants.ERROR_MSG_USERNAME_REQUIRED)
	@Pattern(regexp = BaseConstants.EMAIL_PATTERN)
	private String newusername;

	@NotEmpty(message = UsermgmtServerConstants.ERROR_MSG_PASSWORD_REQUIRED)
	@Size(min = 6, max = 12, message = UsermgmtServerConstants.ERROR_MSG_PASSWORD_RANGE)
	private String newpassword;
	
	@NotEmpty(message = UsermgmtServerConstants.ERROR_MSG_DISPLAYNAME_REQUIRED)
	@Size(max = 30, message = UsermgmtServerConstants.ERROR_MSG_DISPLAYNAME_MAX)
	private String newdisplayname;

	public String getNewusername() {
		return newusername;
	}

	public void setNewusername(String newusername) {
		this.newusername = newusername;
	}

	public String getNewpassword() {
		return newpassword;
	}

	public void setNewpassword(String newpassword) {
		this.newpassword = newpassword;
	}

	public String getNewdisplayname() {
		return newdisplayname;
	}

	public void setNewdisplayname(String newdisplayname) {
		this.newdisplayname = newdisplayname;
	}
}
