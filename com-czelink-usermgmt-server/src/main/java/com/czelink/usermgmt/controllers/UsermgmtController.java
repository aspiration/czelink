package com.czelink.usermgmt.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.czelink.common.intg.entities.User;
import com.czelink.usermgmt.intg.constants.UsermgmtConstants;
import com.czelink.usermgmt.intg.services.UserManagementService;

@Controller
public class UsermgmtController {

	@Resource(name = "userManagementService")
	private UserManagementService userManagementService;

	@RequestMapping("/login")
	@ResponseBody
	String login() {

		// TODO: to implement.
		System.out.println("called login.");

		return "hello world";
	}

	@RequestMapping("/register")
	@ResponseBody
	String register(@RequestParam final String username,
			@RequestParam final String password) {

		// TODO: to implement.
		System.out.println("called register.");

		final User user = new User();
		user.setUsername(username);
		user.setPassword(password);

		final Map<String, Object> context = new HashMap<String, Object>(1);
		final boolean svcResult = this.userManagementService.addNewUser(user,
				context);

		final JSONObject result = new JSONObject();
		result.put("status", svcResult);

		final String errorCode = (String) context
				.get(UsermgmtConstants.EORROR_MSG_CDE);
		if (StringUtils.isNotBlank(errorCode)) {
			result.put("statusCode", errorCode);
		}

		return result.toString();
	}

	public UserManagementService getUserManagementService() {
		return userManagementService;
	}

	public void setUserManagementService(
			UserManagementService userManagementService) {
		this.userManagementService = userManagementService;
	}
}
