package com.czelink.dbaccess;

import org.springframework.data.mongodb.core.MongoOperations;

public interface MongoOperationsAware {

	public void setMongoOperations(final MongoOperations mongoOperations);
}
