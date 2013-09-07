package com.czelink.uploadrepo.intg;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface UploadRepository {

	public boolean saveFile(final Map<String, MultipartFile> files,
			final String subFolder) throws Exception;

	public String getRepositoryContextPath();

}
