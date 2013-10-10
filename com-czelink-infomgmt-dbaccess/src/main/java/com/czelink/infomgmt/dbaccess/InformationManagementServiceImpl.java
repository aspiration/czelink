package com.czelink.infomgmt.dbaccess;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.mongodb.core.MongoOperations;

import com.czelink.infomgmt.intg.entities.InfoArticle;
import com.czelink.infomgmt.intg.services.InformationManagementService;

public class InformationManagementServiceImpl implements
		InformationManagementService, Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * mongoTemplate
	 */
	private transient MongoOperations mongoOperations;

	public List<InfoArticle> retrieveLatestInformationList(final Integer amount) {
		// TODO dummy implementation.
		final List<InfoArticle> infoArticles = this.mongoOperations
				.findAll(InfoArticle.class);

		return infoArticles;
	}

	public InfoArticle retrieveInformationById(String objectId) {
		return this.mongoOperations.findById(objectId, InfoArticle.class);
	}

	public boolean saveNewInfoArticle(InfoArticle infoArticle) {
		this.mongoOperations.insert(infoArticle);
		return true;
	}

	public void setMongoOperations(final MongoOperations pMongoOperations) {
		this.mongoOperations = pMongoOperations;
	}

}
