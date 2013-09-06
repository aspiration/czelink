package com.czelink.uploadrepo.intg;

import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public interface UploadRepository {

	public boolean saveFile(final List<FileItem> fileItems);

	public ServletFileUpload getFileUploadHander(final int maxFileSize,
			final int maxMemSize);
}
