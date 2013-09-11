package com.czelink.usermgmt.intg.handlers;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.czelink.usermgmt.intg.constants.UsermgmtConstants;

public class AjaxAuthenticationSuccessHandler implements
		AuthenticationSuccessHandler {

	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication auth)
			throws IOException, ServletException {

		final String token = UUID.randomUUID().toString();

		final JSONObject result = new JSONObject();
		result.put("status", true);
		result.put("token", token);

		request.getSession().setAttribute(UsermgmtConstants.LOGON_TOKEN_KEY,
				token);

		response.getWriter().print(result.toString());
		response.getWriter().flush();
	}
}
