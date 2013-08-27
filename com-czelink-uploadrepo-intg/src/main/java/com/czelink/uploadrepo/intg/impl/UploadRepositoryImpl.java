package com.czelink.uploadrepo.intg.impl;

import javax.servlet.ServletContext;

import org.springframework.web.context.ServletContextAware;

import com.czelink.uploadrepo.intg.UploadRepository;

public class UploadRepositoryImpl implements UploadRepository,
		ServletContextAware {

	private static final String TARGET_CONTEXT_NAME = "/com-czelink-uploadrepo-facade";

	private ServletContext servletContext;

	public String printAbsolutePath() {
		final ServletContext targetContext = this.servletContext
				.getContext(UploadRepositoryImpl.TARGET_CONTEXT_NAME);

		System.out.println(targetContext.getContextPath());
		System.out.println(targetContext.getRealPath("/"));
		System.out.println(targetContext.getRealPath("."));

		return targetContext.getContextPath();
	}

	public void setServletContext(ServletContext pServletContext) {
		this.servletContext = pServletContext;
	}

}
