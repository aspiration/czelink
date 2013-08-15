package com.czelink.infomgmt.intg.services;

import java.util.List;

import com.czelink.infomgmt.intg.entities.InfoArticle;

public interface InformationManagementService {

	// return type to be defined.
	public List<InfoArticle> retrieveLatestInformationList(final Integer amount);
}
