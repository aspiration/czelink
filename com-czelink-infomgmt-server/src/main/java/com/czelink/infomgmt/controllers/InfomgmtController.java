package com.czelink.infomgmt.controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.czelink.common.intg.entities.User;
import com.czelink.infomgmt.beans.InfoArticleResponse;
import com.czelink.infomgmt.beans.InfoContentResponse;
import com.czelink.infomgmt.beans.NewInfoArticleRequest;
import com.czelink.infomgmt.intg.entities.InfoArticle;
import com.czelink.infomgmt.intg.services.InformationManagementService;
import com.czelink.server.base.beans.JsonBaseViewBean;
import com.czelink.usermgmt.intg.services.UserManagementService;

@Controller
public class InfomgmtController implements Serializable {

	private static final long serialVersionUID = 1L;

	@Resource(name = "baseDateTimeDisplayConverter")
	private transient Converter<Date, String> dateTimeDisplayConverter;

	@Resource(name = "infomgmtService")
	private transient InformationManagementService informationManagementService;

	@Resource(name = "userManagementService")
	private transient UserManagementService userManagementService;

	@RequestMapping(value = "/latestInfo", produces = "application/json")
	public @ResponseBody
	InfoArticleResponse latestInfo() {
		final List<InfoArticle> infoArticle = this.informationManagementService
				.retrieveLatestInformationList(5);

		final InfoArticleResponse response = new InfoArticleResponse();

		final int length = infoArticle.size();
		final List<InfoContentResponse> array = new ArrayList<InfoContentResponse>(
				length);
		for (int i = 0; i < length; i++) {
			final InfoContentResponse infoContent = new InfoContentResponse();
			infoContent.setId(infoArticle.get(i).getId());
			infoContent.setTitle(infoArticle.get(i).getTitle());
			array.add(infoContent);
		}

		response.setContents(array);
		response.setStatus(true);

		return response;
	}

	@RequestMapping(value = "/searchById", produces = "application/json")
	public @ResponseBody
	InfoContentResponse searchById(
			@RequestParam(value = "articleId", required = true) String articleId) {

		final InfoArticle infoArticle = this.informationManagementService
				.retrieveInformationById(articleId);
		final InfoContentResponse infoContent = new InfoContentResponse();

		infoContent.setId(infoArticle.getId());
		infoContent.setTitle(infoArticle.getTitle());

		final String[] paragraphs = infoArticle.getParagraphs();
		final List<String> paraArray = new ArrayList<String>(paragraphs.length);
		for (int j = 0; j < paragraphs.length; j++) {
			paraArray.add(paragraphs[j]);
		}
		infoContent.setStatus(true);
		infoContent.setParagraphs(paraArray);
		infoContent.setAuthor(infoArticle.getAuther().getDisplayName());
		infoContent.setDate(this.dateTimeDisplayConverter.convert(infoArticle
				.getDate()));

		return infoContent;
	}

	@RequestMapping(value = "/saveNewArticle")
	public @ResponseBody
	JsonBaseViewBean saveNewArticle(
			final @RequestBody NewInfoArticleRequest request) {
		final JsonBaseViewBean response = new JsonBaseViewBean();
		response.setStatus(false);

		final InfoArticle infoArticle = new InfoArticle();
		infoArticle.setTitle(request.getTitle().getText());
		infoArticle.setTitlePicUrl(request.getTitle().getPicUrl());
		infoArticle.setParagraphs(request.getParagraphs());
		infoArticle.setParagraphsPicUrl(request.getPicUrls());
		// update the author.
		User user = new User();
		user.setUsername(request.getUserId());
		final Map contextMap = new HashMap();
		user = this.userManagementService.getUserDetail(user, contextMap);
		infoArticle.setAuther(user);
		infoArticle.setDate(new Date());
		infoArticle.setReviewed(Boolean.FALSE);
		final boolean result = this.informationManagementService
				.saveNewInfoArticle(infoArticle);
		response.setStatus(result);

		return response;
	}

	public InformationManagementService getInformationManagementService() {
		return informationManagementService;
	}

	public void setInformationManagementService(
			InformationManagementService informationManagementService) {
		this.informationManagementService = informationManagementService;
	}

	public UserManagementService getUserManagementService() {
		return userManagementService;
	}

	public void setUserManagementService(
			UserManagementService userManagementService) {
		this.userManagementService = userManagementService;
	}

	public Converter<Date, String> getDateTimeDisplayConverter() {
		return dateTimeDisplayConverter;
	}

	public void setDateTimeDisplayConverter(
			Converter<Date, String> dateTimeDisplayConverter) {
		this.dateTimeDisplayConverter = dateTimeDisplayConverter;
	}

}
