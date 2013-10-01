package com.czelink.usermgmt.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.czelink.common.intg.entities.User;
import com.czelink.server.base.beans.JsonBaseViewBean;
import com.czelink.usermgmt.beans.RegisterFormBean;
import com.czelink.usermgmt.intg.constants.UsermgmtConstants;
import com.czelink.usermgmt.intg.services.UserManagementService;

@Controller
public class UsermgmtController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Resource(name = "userManagementService")
	private transient UserManagementService userManagementService;

	@Resource(name = "redisOperations")
	private transient RedisOperations<Object, Object> redisOperations;

	@RequestMapping("/activate")
	String activate(@RequestParam final String uid) {

		String resultStr = "redirect:/?activated";

		final Map context = new HashMap();
		final boolean result = this.userManagementService.activateNewUser(uid,
				context);
		final String verifyKey = UUID.randomUUID().toString();
		if (result) {
			final String username = (String) context
					.get(UsermgmtConstants.USER_NAME);
			this.redisOperations.opsForValue().set(username, verifyKey);
			resultStr = resultStr.concat("=" + username);
		} else {
			resultStr = resultStr.concat("=" + verifyKey);
		}
		return resultStr;
	}

	@RequestMapping(value = "/register", produces = "application/json")
	@ResponseBody
	JsonBaseViewBean register(@Valid final RegisterFormBean registerBean,
			final BindingResult bindingResult,
			final HttpServletRequest httpRequest) {

		final String username = registerBean.getNewusername();
		final String password = registerBean.getNewpassword();
		final String displayname = registerBean.getNewdisplayname();

		final JsonBaseViewBean response = new JsonBaseViewBean();
		boolean svcResult = false;

		if (!bindingResult.hasErrors()) {
			final User user = new User();
			user.setUsername(username);
			user.setPassword(password);
			user.setDisplayName(displayname);

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
				response.setStatusCode(errorCode);
			}
		} else {
			final List<ObjectError> errors = bindingResult.getAllErrors();
			final int length = errors.size();
			final List validationErrors = new ArrayList<String>(length);
			for (int i = 0; i < length; i++) {
				final ObjectError error = errors.get(i);
				validationErrors.add(error.getDefaultMessage());
			}
			response.setValidateErrors(validationErrors);
		}

		response.setStatus(svcResult);

		return response;
	}

	@RequestMapping(value = "/checkActivateStatus", produces = "application/json")
	@ResponseBody
	JsonBaseViewBean checkActivateStatus(
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

		final JsonBaseViewBean response = new JsonBaseViewBean();
		if (StringUtils.isNotBlank(verifyKeyDest)
				&& verifyKeyDest.equals(verifyKeySrc)) {
			response.setStatus(true);
		} else {
			response.setStatus(false);
		}

		return response;
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
