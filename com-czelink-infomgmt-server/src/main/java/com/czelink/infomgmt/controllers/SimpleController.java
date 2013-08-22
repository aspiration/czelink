package com.czelink.infomgmt.controllers;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.czelink.infomgmt.intg.entities.InfoArticle;
import com.czelink.infomgmt.intg.services.InformationManagementService;

@Controller
public class SimpleController {

	@Resource(name = "baseDateTimeDisplayConverter")
	private Converter<Date, String> dateTimeDisplayConverter;

	@Resource(name = "infomgmtService")
	private InformationManagementService informationManagementService;

	@RequestMapping("/simple")
	public @ResponseBody
	String simple() {
		final List<InfoArticle> infoArticle = this.informationManagementService
				.retrieveLatestInformationList(5);

		final JSONObject result = new JSONObject();

		final JSONArray array = new JSONArray();
		for (int i = 0; i < infoArticle.size(); i++) {
			final JSONObject infoContent = new JSONObject();
			infoContent.put("id", infoArticle.get(i).getId());
			infoContent.put("title", infoArticle.get(i).getTitle());
			array.add(infoContent);
		}

		result.put("contents", array);

		return result.toString();
	}

	@RequestMapping("/searchById")
	public @ResponseBody
	String searchById(
			@RequestParam(value = "articleId", required = true) String articleId) {

		final InfoArticle infoArticle = this.informationManagementService
				.retrieveInformationById(articleId);

		final JSONObject infoContent = new JSONObject();

		infoContent.put("id", infoArticle.getId());
		infoContent.put("title", infoArticle.getTitle());

		final String[] paragraphs = infoArticle.getParagraphs();
		final JSONArray paraArray = new JSONArray();
		for (int j = 0; j < paragraphs.length; j++) {
			paraArray.add(paragraphs[j]);
		}
		infoContent.put("paragraphs", paraArray);
		infoContent.put("author", infoArticle.getAuther());
		infoContent.put("date",
				this.dateTimeDisplayConverter.convert(infoArticle.getDate()));

		return infoContent.toString();
	}

	public InformationManagementService getInformationManagementService() {
		return informationManagementService;
	}

	public void setInformationManagementService(
			InformationManagementService informationManagementService) {
		this.informationManagementService = informationManagementService;
	}

	public Converter<Date, String> getDateTimeDisplayConverter() {
		return dateTimeDisplayConverter;
	}

	public void setDateTimeDisplayConverter(
			Converter<Date, String> dateTimeDisplayConverter) {
		this.dateTimeDisplayConverter = dateTimeDisplayConverter;
	}

}
