package com.czelink.infomgmt.dbaccess;

import java.util.List;

import org.springframework.data.mongodb.core.MongoOperations;

import com.czelink.dbaccess.MongoOperationsAware;
import com.czelink.infomgmt.intg.entities.InfoArticle;
import com.czelink.infomgmt.intg.services.InformationManagementService;

public class InformationManagementServiceImpl implements
		InformationManagementService, MongoOperationsAware {

	/**
	 * mongoTemplate
	 */
	private MongoOperations mongoOperations;

	public List<InfoArticle> retrieveLatestInformationList(final Integer amount) {
		// TODO dummy implementation.
		System.out
				.println("print MongoOperations from InformationManagementServiceImpl: "
						+ this.mongoOperations);
		return this.mongoOperations.findAll(InfoArticle.class);
	}

	public void setMongoOperations(final MongoOperations pMongoOperations) {
		this.mongoOperations = pMongoOperations;
	}

}
