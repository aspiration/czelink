package com.czelink.usermgmt.security.handlers;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

public class AjaxAuthenticationFailureHandler implements
		AuthenticationFailureHandler, Serializable {

	private static final long serialVersionUID = 1L;

	public void onAuthenticationFailure(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException {

		final JSONObject result = new JSONObject();
		result.put("status", false);
		result.put("statusCode", "001"); // 001 means login failed.
		result.put("errorMessage", exception.getMessage());

		response.getWriter().print(result.toString());
		response.getWriter().flush();
	}

}
