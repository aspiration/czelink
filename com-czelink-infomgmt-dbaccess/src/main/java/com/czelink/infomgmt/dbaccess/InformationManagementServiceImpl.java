package com.czelink.infomgmt.dbaccess;

import java.util.List;

import org.springframework.data.mongodb.core.MongoOperations;

import com.czelink.infomgmt.intg.entities.InfoArticle;
import com.czelink.infomgmt.intg.services.InformationManagementService;

public class InformationManagementServiceImpl implements
		InformationManagementService {

	/**
	 * mongoTemplate
	 */
	private MongoOperations mongoOperations;

	public List<InfoArticle> retrieveLatestInformationList(final Integer amount) {
		// TODO dummy implementation.
		final List<InfoArticle> infoArticles = this.mongoOperations
				.findAll(InfoArticle.class);

		return infoArticles;
	}

	public InfoArticle retrieveInformationById(String objectId) {
		return this.mongoOperations.findById(objectId, InfoArticle.class);
	}

	public void setMongoOperations(final MongoOperations pMongoOperations) {
		this.mongoOperations = pMongoOperations;
	}

}
