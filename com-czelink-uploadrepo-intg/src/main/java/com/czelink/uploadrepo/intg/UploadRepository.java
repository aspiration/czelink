package com.czelink.uploadrepo.intg;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface UploadRepository {

	public boolean saveFile(final Map<String, MultipartFile> files,
			final String subFolder) throws Exception;

	public boolean saveFile(final MultipartFile file, final String subFolder)
			throws Exception;

	public boolean deleteFile(final String fileName, final String subFolder);

	public String getRepositoryContextPath();

	public String getRepositoryAbsolutePath();

}
