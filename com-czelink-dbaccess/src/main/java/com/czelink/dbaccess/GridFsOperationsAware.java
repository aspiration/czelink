package com.czelink.dbaccess;

import org.springframework.data.mongodb.gridfs.GridFsOperations;

public interface GridFsOperationsAware {
	
	public void setGridFsOperations(final GridFsOperations pGridFsOperations);
}
