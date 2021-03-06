package com.czelink.usermgmt.security.handlers;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.czelink.common.intg.constants.CommonConstants;
import com.czelink.usermgmt.intg.constants.UsermgmtConstants;

public class AjaxAuthenticationSuccessHandler implements
		AuthenticationSuccessHandler, Serializable {

	private static final long serialVersionUID = 1L;

	public void onAuthenticationSuccess(HttpServletRequest request,
			HttpServletResponse response, Authentication auth)
			throws IOException, ServletException {

		final JSONObject result = new JSONObject();
		result.put("status", true);
		result.put("userId", auth.getName());

		final List<GrantedAuthority> authoritiesList = (List<GrantedAuthority>) auth
				.getAuthorities();
		final int size = authoritiesList.size();
		final List<String> rolesList = new ArrayList<String>(size);
		for (int i = 0; i < size; i++) {
			final GrantedAuthority authority = authoritiesList.get(i);
			rolesList.add(authority.getAuthority());
		}
		request.getSession().setAttribute(
				CommonConstants.ROLE_LIST_IN_SESSION_KEY, rolesList);
		request.getSession().setAttribute(CommonConstants.USER_ID,
				auth.getName());

		response.getWriter().print(result.toString());
		response.getWriter().flush();
	}
}
