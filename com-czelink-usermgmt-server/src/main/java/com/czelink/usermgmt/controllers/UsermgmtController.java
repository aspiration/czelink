package com.czelink.usermgmt.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.czelink.common.intg.entities.User;
import com.czelink.usermgmt.beans.RegisterBean;
import com.czelink.usermgmt.intg.constants.UsermgmtConstants;
import com.czelink.usermgmt.intg.services.UserManagementService;

@Controller
public class UsermgmtController {

	@Resource(name = "userManagementService")
	private UserManagementService userManagementService;

	@Resource(name = "redisOperations")
	private RedisOperations<Object, Object> redisOperations;

	@RequestMapping("/activate")
	String activate(@RequestParam final String uid) {

		String resultStr = "redirect:/?activated";

		final Map context = new HashMap();
		final boolean result = this.userManagementService.activateNewUser(uid,
				context);
		if (result) {
			final String username = (String) context
					.get(UsermgmtConstants.USER_NAME);
			final String verifyKey = UUID.randomUUID().toString();
			this.redisOperations.opsForValue().set(username, verifyKey);
			resultStr = resultStr.concat("=" + username);
		} else {
			resultStr = "redirect:/";
		}
		return resultStr;
	}

	@RequestMapping("/register")
	@ResponseBody
	String register(@Valid final RegisterBean registerBean,
			final BindingResult bindingResult,
			final HttpServletRequest httpRequest) {

		final String username = registerBean.getNewusername();
		final String password = registerBean.getNewpassword();

		final JSONObject result = new JSONObject();
		boolean svcResult = false;

		if (!bindingResult.hasErrors()) {
			final User user = new User();
			user.setUsername(username);
			user.setPassword(password);

			// build activated link.
			String activatelinkRoot = httpRequest
					.getParameter("activatelinkRoot");
			if (activatelinkRoot.endsWith("/")) {
				activatelinkRoot = StringUtils.substring(activatelinkRoot, 0,
						activatelinkRoot.length() - 1);
			}

			final Map<String, Object> context = new HashMap<String, Object>(1);
			context.put(UsermgmtConstants.ACTIVATE_URL_KEY, activatelinkRoot);

			svcResult = this.userManagementService.addNewUser(user, context);

			final String errorCode = (String) context
					.get(UsermgmtConstants.EORROR_MSG_CDE);
			if (StringUtils.isNotBlank(errorCode)) {
				result.put("statusCode", errorCode);
			}
		} else {
			final List<ObjectError> errors = bindingResult.getAllErrors();
			final int length = errors.size();
			final JSONArray jsonArr = new JSONArray();
			for (int i = 0; i < length; i++) {
				final ObjectError error = errors.get(i);
				jsonArr.add(error.getDefaultMessage());
			}
			result.put("validateErrors", jsonArr);
		}

		result.put("status", svcResult);

		return result.toString();
	}

	@RequestMapping("/checkActivateStatus")
	@ResponseBody
	String checkActivateStatus(
			@RequestParam(value = "activateInstance") final String activateInstance,
			@RequestParam(value = "verifyKey") final String verifyKeySrc) {

		String verifyKeyDest = StringUtils.EMPTY;

		if (StringUtils.isNotBlank(activateInstance)
				&& StringUtils.isNotBlank(verifyKeySrc)) {
			verifyKeyDest = (String) this.redisOperations.opsForValue().get(
					activateInstance);

			// remove from data store
			this.redisOperations.delete(activateInstance);
		}

		final JSONObject result = new JSONObject();
		if (StringUtils.isNotBlank(verifyKeyDest)
				&& verifyKeyDest.equals(verifyKeySrc)) {
			result.put("status", true);
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

	public RedisOperations<Object, Object> getRedisOperations() {
		return redisOperations;
	}

	public void setRedisOperations(
			RedisOperations<Object, Object> redisOperations) {
		this.redisOperations = redisOperations;
	}
}
