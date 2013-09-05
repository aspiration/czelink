package com.czelink.uploadrepo.intg;

import java.io.InputStream;

public interface UploadRepository {

	public boolean saveFile(final InputStream inputStream);
}
