package com.czelink.usermgmt.controllers;

import javax.annotation.Resource;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.czelink.common.intg.entities.User;
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

		final boolean svcResult = this.userManagementService.addNewUser(user);

		final JSONObject result = new JSONObject();
		result.put("status", svcResult);

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
