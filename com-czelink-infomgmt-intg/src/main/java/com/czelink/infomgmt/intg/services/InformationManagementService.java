package com.czelink.infomgmt.intg.services;

import java.util.List;

import com.czelink.infomgmt.intg.entities.InfoArticle;

public interface InformationManagementService {

	public List<InfoArticle> retrieveLatestInformationList(final Integer amount);

	public InfoArticle retrieveInformationById(final String objectId);

	public boolean saveNewInfoArticle(final InfoArticle infoArticle);
}
