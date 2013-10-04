package com.czelink.uploadrepo.intg.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartFile;

import com.czelink.uploadrepo.intg.UploadRepository;

public class UploadRepositoryImpl implements UploadRepository,
		ServletContextAware, Serializable {

	private static final long serialVersionUID = 1L;

	private static Log LOGGER = LogFactory.getLog(UploadRepositoryImpl.class);

	private static final String TARGET_CONTEXT_NAME = "/com-czelink-uploadrepo-facade";

	private transient ServletContext servletContext;

	private String buildTargetFilePath(final String subFolder,
			final String fileName) {
		String path = this.getRepositoryAbsolutePath();
		if (null != subFolder) {
			path = path.concat("/").concat(subFolder);
		}
		final File dir = new File(path);
		dir.mkdirs();
		if (!path.endsWith("/")) {
			path = path.concat("/");
		}
		path = path.concat(fileName);

		return path;
	}

	private File buildTargetFile(final String subFolder, final String fileName)
			throws IOException {
		final String path = this.buildTargetFilePath(subFolder, fileName);
		final File targetFile = new File(path);
		if (!targetFile.exists()) {
			targetFile.createNewFile();
		}
		return targetFile;
	}

	public String getRepositoryAbsolutePath() {
		final ServletContext targetContext = this.servletContext
				.getContext(UploadRepositoryImpl.TARGET_CONTEXT_NAME);
		String targetPath = StringUtils.replace(targetContext.getRealPath("/"),
				"\\", "/");
		if (targetPath.endsWith("/")) {
			targetPath = StringUtils.substring(targetPath, 0,
					targetPath.length() - 1);
		}
		return targetPath;
	}

	public String getRepositoryContextPath() {
		final ServletContext targetContext = this.servletContext
				.getContext(UploadRepositoryImpl.TARGET_CONTEXT_NAME);
		String targetPath = targetContext.getContextPath();
		if (targetPath.endsWith("/")) {
			targetPath = StringUtils.substring(targetPath, 0,
					targetPath.length() - 1);
		}
		return targetPath;
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
				final File targetFile = this.buildTargetFile(subFolder,
						file.getOriginalFilename());
				file.transferTo(targetFile);
				result = true;
			}
		} catch (IllegalStateException e) {
			if (UploadRepositoryImpl.LOGGER.isErrorEnabled()) {
				UploadRepositoryImpl.LOGGER.error(
						"[FileUploadRepository: " + e.getMessage() + "]", e);
			}
			result = false;
		} catch (IOException e) {
			if (UploadRepositoryImpl.LOGGER.isErrorEnabled()) {
				UploadRepositoryImpl.LOGGER.error(
						"[FileUploadRepository: " + e.getMessage() + "]", e);
			}
			result = false;
		}
		return result;
	}

	public boolean saveFile(MultipartFile file, String subFolder)
			throws Exception {
		boolean result = false;
		try {
			final File targetFile = this.buildTargetFile(subFolder,
					file.getOriginalFilename());
			file.transferTo(targetFile);
			result = true;
		} catch (IOException e) {
			if (UploadRepositoryImpl.LOGGER.isErrorEnabled()) {
				UploadRepositoryImpl.LOGGER.error(
						"[FileUploadRepository: " + e.getMessage() + "]", e);
			}
			result = false;
		}
		return result;
	}

	public boolean deleteFile(String fileName, String subFolder) {
		boolean result = false;
		final String filePath = this.buildTargetFilePath(subFolder, fileName);
		final File file = new File(filePath);
		result = file.delete();
		return result;
	}

}
