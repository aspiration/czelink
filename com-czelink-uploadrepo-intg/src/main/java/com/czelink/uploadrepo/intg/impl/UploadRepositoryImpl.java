package com.czelink.uploadrepo.intg.impl;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
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

	public boolean saveFile(final List<FileItem> fileItems) {
		System.out.println("save file!");
		System.out.println("================= file items ============");
		final Iterator<FileItem> i = fileItems.iterator();
		while (i.hasNext()) {
			FileItem fi = (FileItem) i.next();
			System.out.println(fi.getName() + " - " + fi.getFieldName() + " - "
					+ fi.getContentType());
		}
		System.out.println("================= file items ============");
		return false;
	}

	public ServletFileUpload getFileUploadHander(int maxFileSize, int maxMemSize) {
		final String repoPath = this.getRepositoryAbsolutePath();
		System.out.println(repoPath);
		final DiskFileItemFactory factory = new DiskFileItemFactory();
		// maximum size that will be stored in memory
		factory.setSizeThreshold(maxMemSize);
		// Location to save data that is larger than maxMemSize.
		factory.setRepository(new File(repoPath));
		// Create a new file upload handler
		final ServletFileUpload upload = new ServletFileUpload(factory);
		// maximum file size to be uploaded.
		upload.setSizeMax(maxFileSize);
		return upload;
	}

}
