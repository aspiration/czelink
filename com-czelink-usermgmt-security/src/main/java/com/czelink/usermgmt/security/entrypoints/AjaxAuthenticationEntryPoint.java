package com.czelink.usermgmt.security.entrypoints;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class AjaxAuthenticationEntryPoint implements AuthenticationEntryPoint,
		Serializable {

	private static final long serialVersionUID = 1L;

	public void commence(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException authException)
			throws IOException, ServletException {

		final JSONObject result = new JSONObject();
		result.put("status", false);
		result.put("statusCode", "000"); // 000 means required to login.
		result.put("errorMessage", authException.getMessage());

		response.getWriter().print(result.toString());
		response.getWriter().flush();

	}

}
