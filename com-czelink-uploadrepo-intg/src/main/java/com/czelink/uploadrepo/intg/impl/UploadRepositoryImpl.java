package com.czelink.uploadrepo.intg.impl;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartFile;

import com.czelink.uploadrepo.intg.UploadRepository;

public class UploadRepositoryImpl implements UploadRepository,
		ServletContextAware {

	private static final String TARGET_CONTEXT_NAME = "/com-czelink-uploadrepo-facade";

	private ServletContext servletContext;

	private String getRepositoryAbsolutePath() {
		final ServletContext targetContext = this.servletContext
				.getContext(UploadRepositoryImpl.TARGET_CONTEXT_NAME);
		return targetContext.getRealPath("/");
	}

	public void setServletContext(ServletContext pServletContext) {
		this.servletContext = pServletContext;
	}

	public boolean saveFile(final Map<String, MultipartFile> files,
			final String subFolder) throws IOException {
		boolean result = false;
		try {
			final Set<Entry<String, MultipartFile>> entrySet = files.entrySet();
			for (final Iterator<Entry<String, MultipartFile>> it = entrySet
					.iterator(); it.hasNext();) {
				final Entry<String, MultipartFile> entry = it.next();
				final MultipartFile file = entry.getValue();
				String path = StringUtils.replace(
						this.getRepositoryAbsolutePath(), "\\", "/");
				if (!path.endsWith("/")) {
					path = path.concat("/");
				}
				if (null != subFolder) {
					path = path.concat(subFolder);
				}
				final File dir = new File(path);
				dir.mkdirs();
				path = path.concat("/" + file.getOriginalFilename());
				final File targetFile = new File(path);
				if (!targetFile.exists()) {
					targetFile.createNewFile();
				}
				file.transferTo(targetFile);
				result = true;
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			result = false;
		}
		return result;
	}

	public String getRepositoryContextPath() {
		final ServletContext targetContext = this.servletContext
				.getContext(UploadRepositoryImpl.TARGET_CONTEXT_NAME);
		return targetContext.getContextPath();
	}

}
