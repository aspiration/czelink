package com.czelink.usermgmt.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UsermgmtController {

	@RequestMapping("/login")
	@ResponseBody
	String login() {

		// TODO: to implement.
		System.out.println("called login.");

		return "hello world";
	}
}
