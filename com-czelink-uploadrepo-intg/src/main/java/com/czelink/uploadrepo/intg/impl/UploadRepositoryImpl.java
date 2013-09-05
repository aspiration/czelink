package com.czelink.uploadrepo.intg.impl;

import java.io.InputStream;

import javax.servlet.ServletContext;

import org.springframework.web.context.ServletContextAware;

import com.czelink.uploadrepo.intg.UploadRepository;

public class UploadRepositoryImpl implements UploadRepository,
		ServletContextAware {

	private static final String TARGET_CONTEXT_NAME = "/com-czelink-uploadrepo-facade";

	private ServletContext servletContext;

	private String getRepositoryAbsolutePath() {
		final ServletContext targetContext = this.servletContext
				.getContext(UploadRepositoryImpl.TARGET_CONTEXT_NAME);
		return targetContext.getContextPath();
	}

	public void setServletContext(ServletContext pServletContext) {
		this.servletContext = pServletContext;
	}

	public boolean saveFile(final InputStream inputStream) {
		System.out.println("save file!");
		return false;
	}

}
