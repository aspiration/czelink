package com.czelink.usermgmt.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.czelink.common.intg.entities.User;
import com.czelink.server.base.constants.BaseConstants;
import com.czelink.usermgmt.intg.constants.UsermgmtConstants;
import com.czelink.usermgmt.intg.services.UserManagementService;

@Controller
public class UsermgmtController {

	@Resource(name = "userManagementService")
	private UserManagementService userManagementService;

	@RequestMapping("/activate")
	String activate(@RequestParam final String uid,
			final HttpSession httpSession) {
		final String resultStr = "redirect:/#activated";

		final Map context = new HashMap();
		final boolean result = this.userManagementService.activateNewUser(uid,
				context);
		if (result) {
			httpSession.setAttribute(BaseConstants.ACTIVATED_MARK_KEY,
					Boolean.TRUE);
		} else {
			httpSession.setAttribute(BaseConstants.ACTIVATED_MARK_KEY,
					Boolean.FALSE);
		}
		return resultStr;
	}

	@RequestMapping("/register")
	@ResponseBody
	String register(@RequestParam final String username,
			@RequestParam final String password,
			final HttpServletRequest httpRequest) {

		final User user = new User();
		user.setUsername(username);
		user.setPassword(password);

		// build activated link.
		String activatelinkRoot = httpRequest.getParameter("activatelinkRoot");
		if (activatelinkRoot.endsWith("/")) {
			activatelinkRoot = StringUtils.substring(activatelinkRoot, 0,
					activatelinkRoot.length() - 1);
		}

		final Map<String, Object> context = new HashMap<String, Object>(1);
		context.put(UsermgmtConstants.ACTIVATE_URL_KEY, activatelinkRoot);

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

	@RequestMapping("/checkActivateStatus")
	@ResponseBody
	String checkActivateStatus(final HttpSession session) {
		final Boolean activatedMark = (Boolean) session
				.getAttribute(BaseConstants.ACTIVATED_MARK_KEY);

		final JSONObject result = new JSONObject();
		if (null != activatedMark) {
			result.put("status", activatedMark.booleanValue());
		} else {
			result.put("status", false);
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
